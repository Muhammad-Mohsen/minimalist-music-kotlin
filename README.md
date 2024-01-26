# Minimalist Music

A light weight, folder-based clean music player made in WPF (although there's no MVVM involved). It's by no means a fleshed-out player; There's no playlist management, no equalizer, etc.

## Features
- Supports pretty much all audio file formats (has nothing to do with me, that's just the native Android MediaPlayer)
- Built-in explorer
- Dynamic queues (long-press a music file)
- Audiobook chapters
- Lock screen + Bluetooth controls
- Light/Dark Modes
- Remembers playback state between runs

## Run & Build
Simply install Android Studio, and click the play button!

### RoadMap
- use Media3 stuff!! MediaSessionService + MediaSession.Callback
	- https://developer.android.com/media/implement/playback-app#playing_media_in_the_background
	- https://developer.android.com/media/implement/surfaces/mobile
	- exo chapter metadata??
	- https://www.b4x.com/android/forum/threads/exoplayer-id3-metadata-how-to-get-data.112608/

	- existing players
		https://github.com/timusus/Shuttle
		https://github.com/MuntashirAkon/Metro
		https://github.com/ologe/canaree-music-player
		https://github.com/enricocid/Music-Player-GO


- DONE - settings sheet gesture handling
- DONE - compare perf of service start up: in onStart vs as before + with/without the theme change possible overhead
- DONE - colors of borders similar to inactive text

- DONE settings popup
	- DONE - dark mode selection
	- DONE - update mode on startup
	- DONE - gesture detection
		- move the sheet around
		- determine the delta threshold to hide/slide up the thing
	- DONE - interpolator
	- DONE - theme select
		- how to force theme
			- setTheme()?
		- pause music?
			- it already pauses
		- (dynamic colors)[https://developer.android.com/develop/ui/views/theming/dynamic-colors]
		- DONE - seek jump amount
		- DONE - shuffle/repeat
		- DONE - privacy policy link
			- Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com"));
			  startActivity(browserIntent);
- Compose instead of XML
