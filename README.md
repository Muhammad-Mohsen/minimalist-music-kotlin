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
- IN PROGRESS settings popup
  - dark mode selection
  - update mode on startup
  - gesture detection
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
- Exo Player
  - metadata (has chapters?)
  - replace MediaPlayer
- Compose instead of XML