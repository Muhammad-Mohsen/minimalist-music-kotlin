# Dev Guide | Minimalist Music Kotling - Hybrid

## General
- use a webview for the UI
	- webview will only be responsible for the UI. i.e. playback, metadata, notification, media session management, etc. will still be managed natively
	- this greatly facilitates adding new features and also keeping the code modern without resorting to jetpack compose :D
- DONE - design the new UI
- DONE - create a new Android project using the latest project template (check online to see if there are better templates than the ones in Android Studio)
- DONE - use edge to edge + fallback gracefully
- implement a 'decent' JS bridge
- implement the UI!
	- start with interactions
		- DONE - pulse animation for the play/pause button
		- DONE - subtle scale animation for everything else (.9x scale should be good enough)
		- maybe a ripple as well?
		- DONE - the interaction should be on the :active state, not hover/focus
	- if art,
		- playing: full-color | paused: grayscale!
		- fade in/out when changing
	- create a smooth progressbar animation?
		- can be done with typed css variables?
	- then, there's the state!
		- title + subtitle
		- current time + duration
		- active queue
		- mode (normal/selection/search/selection+search)

## Technical
- DONE - edge to edge
	- DONE - https://developer.android.com/develop/ui/views/layout/edge-to-edge-manually
	- DONE - enable the thing
	- DONE - set the system bars color to transparent
    - DONE - inset handling
- DONE - js bridge (EventBus)
	- js + css bundling: simple bat script :)
	- font(s)
	- sending album art
  ```java
    private static void openJpeg(WebView web, byte[] image) {
      String b64Image = Base64.encode(image, Base64.DEFAULT);
      String html = String.format(HTML_FORMAT, b64Image);
      web.loadData(html, "text/html", "utf-8");
    }
    ```
	- DONE - posting messages
- DONE - pulse animation
- DONE - webview guides (not sure if any good! but it's a start)
	- https://medium.com/@oktaygenc/integrating-webview-in-jetpack-compose-managing-web-content-in-modern-android-1803c56a2da7
	- use onPageFinished
- DONE - need some res
	- notification icons + colors (use the SVGs)
	- some strings for notification channel name, description, etc
- [fonts](https://www.flowbase.co/blog/top-20-free-fonts-for-ui-designers-2025)
- DONE - refactor
	- DONE - `Track` object in `State` to be its own thing (similar to `Playlist`)
    - DONE - `bitmap` encoding
    - DONE - time formatting functions to `GeneralExtensions.kt`
- DONE - move relevant stuff from settings to activity
	- sleep timer start/stop
	- privacy policy click
	- eq click

- develop UI using mock state
	- DONE - header
		- DONE - listing
		- DONE - click handlers
		- DONE - styling
		- DONE - mode changes (at the end)
	- DONE - explorer
		- DONE - lising
		- DONE - styling
		- DONE - filtering
		- DONE - touch handler
		- DONE - mode changes
	- DONE - controls
		- title + album + artist + duration + seek + chapters + art
	- dialogs
		- chapters
		- lyrics
		- settings
- serialization
	- https://medium.com/@midoripig1009/working-with-json-in-kotlin-parsing-and-serialization-a62300ec43b8
	- JSONObject
		- https://stackoverflow.com/questions/22685281/parsing-json-in-android-with-out-using-any-external-libraries

- DONE - directory change animation
	- new: slide + old: scale down + blur?
	- https://css-tricks.com/different-approaches-for-creating-a-staggered-animation/

- serialize the state
	- move shuffle/repeat to root state object
	- add sort to root state object
	- add lyrics
	- add full chapter info
	- encode art to base64

	- BIG MILESTONE - handle the serialized state in the UI

- update eventbus events in native
- implement sort comparer
- implement dialog UI
	- chapters -> needs a seek to event (already exists)
	- lyrics
	- settings -> a lot of events :D

- BIG MILESTONE - test-run the communication...directory change

- app launch
	- init the state
	- init webview
	- check storage permission
	- init native
	- serialize the state and send it to webview

- back navigation
	- the native code will decide and send the parent dir to the webview if necessary
	- need to check the mode first before changing dir
- cheat off of kotlin / tauri code

- PERMISSION - update the mode on the document

## Chromatic Aberration
can look cool with the `play-pause` button
```html
<svg width="0" height="0">
	<filter id="kill">
		<feColorMatrix type="matrix" result="red_" values="4 0 0 0 0
					0 0 0 0 0
					0 0 0 0 0
					0 0 0 1 0" />
		<feOffset in="red_" dx="2" dy="0" result="red" />
		<feColorMatrix type="matrix" in="SourceGraphic" result="blue_" values="0 0 0 0 0
					0 3 0 0 0
					0 0 10 0 0
					0 0 0 1 0" />
		<feOffset in="blue_" dx="-3" dy="0" result="blue" />
		<feBlend mode="screen" in="red" in2="blue" />

	</filter>
</svg>
```

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

- [dynamic colors](https://developer.android.com/develop/ui/views/theming/dynamic-colors)
- equalizer
	- issue with getting to the session ID from the controls
	- issue with getting to the activity from the service!!
	- so, put a reference to the activity in the state!
		- replace the context with the activity
		- can't because I'm sometimes initializing it from the service
	- fuck this
	- appears not to make any difference :D :D
- DONE - playback rate
	- a lot more straightforward
	- need to update the state
	- need to update the media session
- DONE - icons
	- DONE - eq icon 4 bars with increased spacing
	- DONE - playback speed smaller play icon
	- DONE - notification icon smaller

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