using System.Text.Json;

namespace TrapClient.Launcher;

internal sealed class LauncherSettings
{
	private static readonly string SettingsDirectory = Path.Combine(
		Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),
		"TRAP CLIENT"
	);
	private static readonly string SettingsFile = Path.Combine(SettingsDirectory, "launcher.json");

	public string MicrosoftClientId { get; set; } = "";

	public string GameDirectory { get; set; } = Path.Combine(SettingsDirectory, "game");

	public static LauncherSettings Load()
	{
		if (!File.Exists(SettingsFile))
		{
			return new LauncherSettings();
		}

		try
		{
			using var stream = File.OpenRead(SettingsFile);
			return JsonSerializer.Deserialize<LauncherSettings>(stream) ?? new LauncherSettings();
		}
		catch (JsonException exception)
		{
			throw new InvalidDataException($"The launcher settings file is invalid: {SettingsFile}", exception);
		}
	}

	public void Save()
	{
		Directory.CreateDirectory(SettingsDirectory);
		var temporaryFile = $"{SettingsFile}.{Guid.NewGuid():N}.tmp";
		try
		{
			using (var stream = File.Create(temporaryFile))
			{
				JsonSerializer.Serialize(stream, this, new JsonSerializerOptions { WriteIndented = true });
			}

			File.Move(temporaryFile, SettingsFile, overwrite: true);
		}
		finally
		{
			if (File.Exists(temporaryFile))
			{
				File.Delete(temporaryFile);
			}
		}
	}
}
