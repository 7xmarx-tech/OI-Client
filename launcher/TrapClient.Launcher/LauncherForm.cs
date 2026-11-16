using System.Diagnostics;
using System.Net.Http;
using System.Security.Cryptography;
using CmlLib.Core;
using CmlLib.Core.Auth.Microsoft;
using CmlLib.Core.ModLoaders.FabricMC;
using CmlLib.Core.ProcessBuilder;
using XboxAuthNet.Game.Msal;

namespace TrapClient.Launcher;

internal sealed class LauncherForm : Form
{
	private const string MinecraftVersion = "26.1.2";
	private const string FabricLoaderVersion = "0.20.1";
	private const string FabricApiUrl =
		"https://cdn.modrinth.com/data/P7dR8mSH/versions/3dM0X6ou/fabric-api-0.155.3%2B26.1.2.jar";
	private const string FabricApiSha512 =
		"fa0308b42e9cd0dc3ff946f246d199d74547e0c946d7e3c9780a2537155307baf25129f69bd1349273babf879ffb4608b7e4af0740938f9c0f7b371b1c67d6ca";
	private const string EmbeddedClientJar = "TrapClient.Launcher.TrapClient-26.1.2.jar";

	private readonly LauncherSettings settings;
	private readonly TextBox clientIdInput = new();
	private readonly Label statusLabel = new();
	private readonly Label accountLabel = new();
	private readonly Button signInButton = new();
	private readonly Button playButton = new();
	private readonly ProgressBar progressBar = new();
	private CmlLib.Core.Auth.MSession? session;
	private bool isGameRunning;

	public LauncherForm()
	{
		settings = LauncherSettings.Load();
		Text = "TRAP CLIENT";
		Width = 920;
		Height = 600;
		MinimumSize = new Size(780, 520);
		StartPosition = FormStartPosition.CenterScreen;
		BackColor = Color.FromArgb(13, 11, 18);
		ForeColor = Color.FromArgb(239, 234, 247);
		Font = new Font("Segoe UI", 10F);

		BuildInterface();
	}

	private void BuildInterface()
	{
		var sidebar = new Panel
		{
			Dock = DockStyle.Left,
			Width = 260,
			BackColor = Color.FromArgb(23, 19, 31),
			Padding = new Padding(24)
		};
		Controls.Add(sidebar);

		var logo = new Label
		{
			Text = "TRAP",
			Font = new Font("Segoe UI", 24F, FontStyle.Bold),
			ForeColor = Color.FromArgb(190, 143, 255),
			AutoSize = true,
			Location = new Point(24, 28)
		};
		sidebar.Controls.Add(logo);

		var clientWord = new Label
		{
			Text = "CLIENT",
			Font = new Font("Segoe UI", 11F, FontStyle.Bold),
			ForeColor = Color.FromArgb(237, 230, 249),
			AutoSize = true,
			Location = new Point(28, 72)
		};
		sidebar.Controls.Add(clientWord);

		var version = new Label
		{
			Text = "MINECRAFT 26.1.2  •  FABRIC",
			Font = new Font("Segoe UI", 8.5F),
			ForeColor = Color.FromArgb(139, 130, 151),
			AutoSize = true,
			Location = new Point(28, 102)
		};
		sidebar.Controls.Add(version);

		var main = new Panel
		{
			Dock = DockStyle.Fill,
			Padding = new Padding(40, 35, 40, 30)
		};
		Controls.Add(main);
		main.BringToFront();

		var heading = new Label
		{
			Text = "Your Minecraft, refined.",
			Font = new Font("Segoe UI Semibold", 23F, FontStyle.Bold),
			AutoSize = true,
			Location = new Point(40, 34)
		};
		main.Controls.Add(heading);

		var subtitle = new Label
		{
			Text = "Sign in with Microsoft, then install or launch TRAP CLIENT.",
			ForeColor = Color.FromArgb(151, 144, 163),
			AutoSize = true,
			Location = new Point(42, 78)
		};
		main.Controls.Add(subtitle);

		var accountHeading = CreateSectionLabel("MICROSOFT ACCOUNT", 42, 145);
		main.Controls.Add(accountHeading);

		accountLabel.Text = "Not signed in";
		accountLabel.ForeColor = Color.FromArgb(184, 176, 198);
		accountLabel.AutoSize = true;
		accountLabel.Location = new Point(42, 174);
		main.Controls.Add(accountLabel);

		ConfigureButton(signInButton, "SIGN IN", 42, 211, 142, SignInClicked);
		main.Controls.Add(signInButton);

		var installHeading = CreateSectionLabel("YOUR CLIENT", 42, 293);
		main.Controls.Add(installHeading);

		var gamePath = new Label
		{
			Text = settings.GameDirectory,
			ForeColor = Color.FromArgb(151, 144, 163),
			AutoEllipsis = true,
			Width = Math.Max(220, main.ClientSize.Width - 95),
			Anchor = AnchorStyles.Top | AnchorStyles.Left | AnchorStyles.Right,
			Location = new Point(42, 322)
		};
		main.Controls.Add(gamePath);

		ConfigureButton(playButton, "PLAY", 42, 365, 210, PlayClicked);
		playButton.Height = 54;
		playButton.Font = new Font("Segoe UI", 12F, FontStyle.Bold);
		playButton.Enabled = false;
		main.Controls.Add(playButton);

		var settingsHeading = CreateSectionLabel("MICROSOFT APP CLIENT ID", 42, 445);
		main.Controls.Add(settingsHeading);

		clientIdInput.Text = settings.MicrosoftClientId;
		clientIdInput.PlaceholderText = "Configure your public OAuth client ID";
		clientIdInput.BackColor = Color.FromArgb(31, 26, 40);
		clientIdInput.ForeColor = Color.FromArgb(239, 234, 247);
		clientIdInput.BorderStyle = BorderStyle.FixedSingle;
		clientIdInput.Width = Math.Max(240, main.ClientSize.Width - 95);
		clientIdInput.Anchor = AnchorStyles.Top | AnchorStyles.Left | AnchorStyles.Right;
		clientIdInput.Location = new Point(42, 473);
		clientIdInput.TextChanged += (_, _) => SaveClientId();
		main.Controls.Add(clientIdInput);

		statusLabel.Text = "Ready. Microsoft sign-in requires your registered public app client ID.";
		statusLabel.ForeColor = Color.FromArgb(166, 157, 181);
		statusLabel.AutoEllipsis = true;
		statusLabel.Width = Math.Max(240, main.ClientSize.Width - 95);
		statusLabel.Anchor = AnchorStyles.Left | AnchorStyles.Right | AnchorStyles.Bottom;
		statusLabel.Location = new Point(42, main.ClientSize.Height - 41);
		main.Controls.Add(statusLabel);

		progressBar.Style = ProgressBarStyle.Marquee;
		progressBar.MarqueeAnimationSpeed = 25;
		progressBar.Visible = false;
		progressBar.Height = 4;
		progressBar.Width = Math.Max(240, main.ClientSize.Width - 95);
		progressBar.Anchor = AnchorStyles.Left | AnchorStyles.Right | AnchorStyles.Bottom;
		progressBar.Location = new Point(42, main.ClientSize.Height - 12);
		main.Controls.Add(progressBar);

		main.Resize += (_, _) =>
		{
			gamePath.Width = Math.Max(220, main.ClientSize.Width - 95);
			clientIdInput.Width = Math.Max(240, main.ClientSize.Width - 95);
			statusLabel.Width = Math.Max(240, main.ClientSize.Width - 95);
			progressBar.Width = Math.Max(240, main.ClientSize.Width - 95);
			statusLabel.Top = main.ClientSize.Height - 41;
			progressBar.Top = main.ClientSize.Height - 12;
		};
	}

	private static Label CreateSectionLabel(string text, int x, int y) => new()
	{
		Text = text,
		Font = new Font("Segoe UI", 8.5F, FontStyle.Bold),
		ForeColor = Color.FromArgb(167, 146, 198),
		AutoSize = true,
		Location = new Point(x, y)
	};

	private void ConfigureButton(Button button, string text, int x, int y, int width, EventHandler handler)
	{
		button.Text = text;
		button.Location = new Point(x, y);
		button.Width = width;
		button.Height = 42;
		button.FlatStyle = FlatStyle.Flat;
		button.FlatAppearance.BorderSize = 0;
		button.BackColor = Color.FromArgb(116, 72, 172);
		button.ForeColor = Color.White;
		button.Cursor = Cursors.Hand;
		button.Click += handler;
	}

	private async void SignInClicked(object? sender, EventArgs e)
	{
		SaveClientId();
		if (!Guid.TryParse(settings.MicrosoftClientId, out _))
		{
			ShowClientIdInstructions();
			return;
		}

		SetBusy(true, "Starting secure Microsoft sign-in…");
		try
		{
			var app = await MsalClientHelper.BuildApplicationWithCache(settings.MicrosoftClientId);
			var loginHandler = new JELoginHandlerBuilder().Build();
			var authenticator = loginHandler.CreateAuthenticatorWithNewAccount();
			authenticator.AddMsalOAuth(app, msal => msal.DeviceCode(code =>
			{
				SetStatus(code.Message);
				if (!IsDisposed && IsHandleCreated)
				{
					BeginInvoke(() => MessageBox.Show(
						this,
						code.Message,
						"Sign in to TRAP CLIENT",
						MessageBoxButtons.OK,
						MessageBoxIcon.Information
					));
				}

				return Task.CompletedTask;
			}));
			authenticator.AddXboxAuthForJE(xbox => xbox.Basic());
			authenticator.AddJEAuthenticator();

			session = await authenticator.ExecuteForLauncherAsync();
			accountLabel.Text = $"Signed in as {session.Username}";
			accountLabel.ForeColor = Color.FromArgb(166, 224, 187);
			playButton.Enabled = true;
			SetStatus("Account verified. TRAP CLIENT is ready.");
		}
		catch (Exception exception)
		{
			ShowOperationError("Microsoft sign-in failed", exception);
		}
		finally
		{
			SetBusy(false);
		}
	}

	private async void PlayClicked(object? sender, EventArgs e)
	{
		if (session is null)
		{
			SetStatus("Sign in with your Microsoft account before launching.");
			return;
		}

		SetBusy(true, "Preparing Minecraft 26.1.2 and Fabric…");
		try
		{
			Directory.CreateDirectory(settings.GameDirectory);
			var gamePath = new MinecraftPath(settings.GameDirectory);
			var fabricVersion = FabricInstaller.GetVersionName(MinecraftVersion, FabricLoaderVersion);
			using var http = new HttpClient();
			var fabricInstaller = new FabricInstaller(http);
			await fabricInstaller.Install(MinecraftVersion, FabricLoaderVersion, gamePath);

			var launcher = new MinecraftLauncher(gamePath);
			SetStatus("Downloading and validating official Minecraft files…");
			await launcher.InstallAsync(fabricVersion);

			SetStatus("Installing TRAP CLIENT and verifying Fabric API…");
			await InstallClientFilesAsync(settings.GameDirectory);

			SetStatus("Launching TRAP CLIENT…");
			var process = await launcher.CreateProcessAsync(fabricVersion, new MLaunchOption
			{
				Session = session,
				MaximumRamMb = 4096
			});
			process.EnableRaisingEvents = true;
			process.Exited += (_, _) =>
			{
				if (IsDisposed || !IsHandleCreated)
				{
					return;
				}

				BeginInvoke(() =>
				{
					isGameRunning = false;
					playButton.Enabled = session is not null;
					SetStatus("Minecraft closed.");
				});
			};
			isGameRunning = true;
			if (!process.Start())
			{
				isGameRunning = false;
				throw new InvalidOperationException("Minecraft's process could not be started.");
			}
			SetStatus("Minecraft is running. You can close the launcher window.");
		}
		catch (Exception exception)
		{
			isGameRunning = false;
			ShowOperationError("Could not launch TRAP CLIENT", exception);
		}
		finally
		{
			SetBusy(false);
		}
	}

	private async Task InstallClientFilesAsync(string gameDirectory)
	{
		var modsDirectory = Path.Combine(gameDirectory, "mods");
		Directory.CreateDirectory(modsDirectory);

		using (Stream? resource = typeof(LauncherForm).Assembly.GetManifestResourceStream(EmbeddedClientJar))
		{
			if (resource is null)
			{
				throw new FileNotFoundException("The TRAP CLIENT mod is missing from this launcher build.");
			}

			var clientPath = Path.Combine(modsDirectory, "TrapClient-26.1.2.jar");
			await WriteAtomicallyAsync(clientPath, output => resource.CopyToAsync(output));
		}

		using var http = new HttpClient();
		var apiBytes = await http.GetByteArrayAsync(FabricApiUrl);
		var actualHash = Convert.ToHexStringLower(SHA512.HashData(apiBytes));
		if (!CryptographicOperations.FixedTimeEquals(
				Convert.FromHexString(actualHash),
				Convert.FromHexString(FabricApiSha512)))
		{
			throw new InvalidDataException("The Fabric API download failed its SHA-512 integrity check.");
		}

		var apiPath = Path.Combine(modsDirectory, "fabric-api-0.155.3+26.1.2.jar");
		await WriteAtomicallyAsync(apiPath, output => output.WriteAsync(apiBytes).AsTask());
	}

	private static async Task WriteAtomicallyAsync(string destinationPath, Func<Stream, Task> writeAsync)
	{
		var temporaryPath = $"{destinationPath}.{Guid.NewGuid():N}.download";
		try
		{
			await using (var output = new FileStream(
				temporaryPath,
				FileMode.CreateNew,
				FileAccess.Write,
				FileShare.None,
				bufferSize: 81920,
				useAsync: true))
			{
				await writeAsync(output);
			}

			File.Move(temporaryPath, destinationPath, overwrite: true);
		}
		finally
		{
			if (File.Exists(temporaryPath))
			{
				File.Delete(temporaryPath);
			}
		}
	}

	private void SaveClientId()
	{
		settings.MicrosoftClientId = clientIdInput.Text.Trim();
		try
		{
			settings.Save();
		}
		catch (IOException exception)
		{
			SetStatus($"Could not save launcher settings: {exception.Message}");
		}
	}

	private void ShowClientIdInstructions()
	{
		MessageBox.Show(
			this,
			"Register a public (no client secret) Microsoft Entra application for TRAP CLIENT, enable public client/device-code authentication, then paste its Application (client) ID here. The launcher never asks for or stores your Microsoft password.",
			"Microsoft sign-in setup",
			MessageBoxButtons.OK,
			MessageBoxIcon.Information
		);
	}

	private void ShowOperationError(string title, Exception exception)
	{
		SetStatus($"{title}: {exception.Message}");
		MessageBox.Show(this, exception.ToString(), title, MessageBoxButtons.OK, MessageBoxIcon.Error);
	}

	private void SetBusy(bool busy, string? message = null)
	{
		signInButton.Enabled = !busy;
		playButton.Enabled = !busy && session is not null && !isGameRunning;
		clientIdInput.Enabled = !busy;
		progressBar.Visible = busy;
		if (message is not null)
		{
			SetStatus(message);
		}
	}

	private void SetStatus(string text)
	{
		if (statusLabel.InvokeRequired)
		{
			statusLabel.BeginInvoke(() => SetStatus(text));
			return;
		}

		statusLabel.Text = text;
	}
}
