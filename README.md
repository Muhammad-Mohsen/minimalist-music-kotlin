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

- Compose instead of XML
- (dynamic colors)[https://developer.android.com/develop/ui/views/theming/dynamic-colors]

- DONE - settings sheet gesture handling
- DONE settings popup
	- DONE - dark mode selection
	- DONE - update mode on startup
	- DONE - gesture detection
		- move the sheet around
		- determine the delta threshold to hide/slide up the thing
	- DONE - interpolator
	- DONE - theme select
    - DONE - seek jump amount
    - DONE - shuffle/repeat
    - DONE - privacy policy link
        - Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com"));
          startActivity(browserIntent);
  - DONE - compare perf of service start up: in onStart vs as before + with/without the theme change possible overhead
  - DONE - colors of borders similar to inactive text

- DONE - search mode
  - DONE - update icon
  - add exit animation sequence (flip of the original)
  - when it activates
    - breadcrumb bar mode changes
    - track count, add, and play selected buttons should be hidden
    - edit text should take focus
    - controls should hide (explorer should extend all the way down)
  - when it deactivates
    - edit text should blur
  - actual search function
    - add hidden to file model
    - if hidden, should animateItemRemoved
  - if active and the multiselect mode is activated, the track count, add, and play selected buttons will be displayed and track count text should change from "x track(s) selected" to "(x)"
  - if multiselect mode is active, the edit text will be displayed and track count text should change...