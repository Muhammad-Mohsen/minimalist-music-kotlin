class MusicControls extends HTMLElementBase {

	#TARGET = EventBus.Target.CONTROLS;

	connectedCallback() {
		this.#render();
		EventBus.subscribe((event) => this.#handler(event));
	}

	#handler(event) {
		if (event.target == this.#TARGET) return;

		when(event.type)
			.is(EventBus.Type.MODE_CHANGE, () => {
				this.searchButton.classList.remove('ic-close'); // TODO fugly shit
				this.searchButton.classList.add('ic-search');
			})
			.is(EventBus.Type.PLAY_TRACK, async () => {
				// const path = State.get(State.Key.TRACK);

				// load(path, 'autoplay');
				// Playlist.set(await Explorer.listTracks());
			})
			.is(EventBus.Type.RESTORE_STATE, async () => {
				// const path = State.get(State.Key.TRACK);
				// const currentTime = parseInt(State.get(State.Key.SEEK)) || 0;
				// const duration = parseInt(State.get(State.Key.DURATION)) || 100;

				// seek(currentTime, duration);

				// onVolumeChange(parseFloat(State.get(State.Key.VOLUME)));
				// shuffle(State.get(State.Key.SHUFFLE));
				// repeat(State.get(State.Key.REPEAT));

				// // loadeddata event, apparently, doesn't fire until the audio needs to be played! So if autoplay is false, it won't fire
				// MetadataWorker.postMessage(Native.FS.pathToSrc(path));
			})
			.is(EventBus.Type.PLAY, () => playPause(true, 'suppress'))
			.is(EventBus.Type.PAUSE, () => playPause(false, 'suppress'))
			.is(EventBus.Type.PLAY_NEXT, () => playNext(false))
			.is(EventBus.Type.PLAY_PREV, () => playPrev())
			.is(EventBus.Type.PLAY_PAUSE, () => playPause())
			.is(EventBus.Type.FF, () => ff())
			.is(EventBus.Type.RW, () => rw());
	}

	playPause(play) {
		play = play != undefined ? play : !this.playPauseButton.hasAttribute('playing');
		this.playPauseButton.toggleAttribute('playing', play);
		EventBus.dispatch({ type: play ? EventBus.Type.PLAY : EventBus.Type.PAUSE, target: this.#TARGET });

		setTimeout(() => this.pausePath.style.strokeDashoffset = parseInt(this.pausePath.style.strokeDashoffset) + 75, play ? 200 : 0);
		setTimeout(() => this.playPath.style.strokeDashoffset = parseInt(this.playPath.style.strokeDashoffset) + 80, play ? 0 : 200);
	}
	playNext() {
		EventBus.dispatch({ type: EventBus.Type.PLAY_NEXT, target: this.#TARGET });
	}
	playPrevious() {
		EventBus.dispatch({ type: EventBus.Type.PLAY_PREV, target: this.#TARGET });
	}

	updateMetadata(metadata) {
		this.seekDuration.innerHTML = '';
		this.seekRange.max = '';

		this.trackTitle.innerHTML = '';
		this.trackAlbumArtist.innerHTML = '';

		this.chapters.innerHTML = '';
	}

	updateSeek(current, duration) {
		this.seekCurrent.innerHTML = '';
		this.seekRange.innerHTML = '';
	}
	onSeekChange() {

	}

	toggleSearch() {
		this.searchButton.classList.toggle('ic-search');
		this.searchButton.classList.toggle('ic-close');

		state.mode = when(state.mode)
			.is(state.Mode.NORMAL, () => state.Mode.SEARCH)
			.is(state.Mode.SEARCH, () => state.Mode.NORMAL)
			.is(state.Mode.SELECT, () => state.Mode.SEARCH_SELECT)
			.is(state.Mode.SEARCH_SELECT, () => state.Mode.SELECT)
			.val();

		EventBus.dispatch({ type: EventBus.Type.MODE_CHANGE, target: this.#TARGET });
	}

	#render() {
		super.render(`
			<img alt="Album Art" id="art">
			<div class="main-controls">
				<input type="range" id="seek-range">

				<button id="play-pause-button" class="ic-btn" aria-label="play/pause" onclick="${this.handle}.playPause()">
					<svg  width="180" height="15" fill="none" xmlns="http://www.w3.org/2000/svg">
						<path id="pause-path" style="stroke-dashoffset: 75;" d="M94 15V7.5H180M85 0V7.5H0" stroke="var(--foreground)"/>
						<path id="play-path" style="stroke-dashoffset: 0;" d="M83.0469 1L95.9531 7.5H180M83.0469 15V7.5H0" stroke="var(--foreground)"/>
					</svg>
				</button>

				<div class="seek-text">
					<output id="seek-current">12:14</output>
					<output id="seek-duration">50:23</output>
				</div>

				<ul id="chapters"></ul>
				<h1 id="track-title">Alien Isolation.mp3</h1>
				<h2 id="track-album-artist"><strong>Keith R.A. DeCandido</strong> | Sarah Mollo-Christensen</h2>
			</div>

			<div class="secondary-controls">
				<button id="search-button" class="ic-btn ic-search" onclick="${this.handle}.toggleSearch();" aria-label="search"></button>
				<button id="chapters-button" class="ic-btn ic-chapters" aria-label="chapters"></button>
				<button id="previous-button" class="ic-btn ic-prev" aria-label="previous"></button>
				<button id="next-button" class="ic-btn ic-next" aria-label="next"></button>
				<button id="lyrics-button" class="ic-btn ic-lyrics" aria-label="lyrics"></button>
				<button id="more-button" class="ic-btn ic-more" aria-label="more"></button>
			</div>
		`);
	}
}

customElements.define('music-controls', MusicControls);

// IMPORTS
// MetadataWorker.addEventListener('message', (event) => {
// 	const metadata = JSON.parse(event.data);
// 	albumArtist(metadata.album, metadata.artist);
// 	artwork(metadata.artwork);
// 	seek(audio.currentTime || 0, metadata.duration || audio.duration); // the metadata library reported NaN for absolution.m4b!

// 	EventBus.dispatch({
// 		target: EventBus.target.PLAYER,
// 		type: EventBus.type.METADATA_UPDATE,
// 		data: metadata
// 	});

// 	loadingIndicator(false);
// });

// audio.onended = function () {
// 	playNext(true);
// }
// audio.onloadeddata = function () {
// 	playPause(audio.autoplay);
// 	MetadataWorker.postMessage({ type: EventBus.type.METADATA_FETCH, src: audio.src }); // fetch metadata after audio is loaded so as not to trip over each other
// }
// audio.ontimeupdate = function () {
// 	if (!ui.seek.hasAttribute(SEEKING_ATTR)) seek(audio.currentTime);
// }

function load(path, autoplay) {
	if (!initialized()) return albumArtist(State.get(State.Key.ALBUM)); // show quote

	loadingIndicator(true);
	const src = Native.FS.pathToSrc(path);

	audio.pause();
	seek(0);
	audio.src = src;
	audio.autoplay = !!autoplay;
	title(); // immediately show title (while waiting for the metadata)
}

// PLAYBACK CONTROLS
function playPause(force, suppress) {
	if (!initialized()) return;

	force != undefined
		? (force ? audio.play() : audio.pause())
		: (audio.paused ? audio.play() : audio.pause());

	ui.playPause.classList.toggle('pause', !audio.paused);
	progressBar('force');

	if (!suppress) EventBus.dispatch({ type: force ? EventBus.Type.PLAY : EventBus.Type.PAUSE, target: SELF });
}
function playNext(onComplete) {
	if (!initialized()) return;

	const path = Playlist.getNext(onComplete);
	if (!path) return;

	State.set(State.Key.TRACK, path);
	load(path, 'autoplay');
	EventBus.dispatch({ type: EventBus.Type.PLAY_TRACK, target: SELF });
}
function playPrev() {
	if (!initialized()) return;

	const path = Playlist.getPrev();
	if (!path) return;

	State.set(State.Key.TRACK, path);
	load(path, 'autoplay');
	EventBus.dispatch({ type: EventBus.Type.PLAY_TRACK, target: SELF });
}
function ff() {
	audio.currentTime += SEEK_JUMP
	seek(audio.currentTime);
}
function rw() {
	audio.currentTime -= SEEK_JUMP
	seek(audio.currentTime);
}
function shuffle(force) {
	const current = Playlist.toggleShuffle(force, force != undefined);
	ui.shuffle.innerHTML = current ? 'shuffle_on' : 'shuffle';

}
function repeat(force) {
	const current = Playlist.toggleRepeat(force, force != undefined);
	ui.repeat.innerHTML = when(current)
		.is(0, () => 'repeat')
		.is(1, () => 'repeat_on')
		.is(2, () => 'repeat_one_on')
		.val();
}

// VOLUME
function onVolumeChange(restoredVal) {
	const val = (isNaN(restoredVal) || restoredVal == undefined) ? ui.volume.value : restoredVal;

	if (restoredVal != undefined) ui.volume.value = val; // update the vol if restored
	else State.set(State.Key.VOLUME, val); // update the state otherwise

	audio.volume = val;
	if (val) audio.muted = false;
}
function toggleMute() {
	audio.muted = !audio.muted;
	ui.volumeIcon.innerHTML = audio.muted ? 'volume_off' : 'volume_up';
}

// SEEK
function seek(position, duration) {
	if (!initialized()) return;

	if (duration) {
		ui.seek.max = duration;
		ui.duration.innerHTML = readableTime(duration);
		State.set(State.Key.DURATION, duration);
	}

	ui.position.innerHTML = readableTime(position);
	ui.seek.value = position;
	State.set(State.Key.SEEK, position);

	progressBar();
}
function onSeekMouseDown() {
	ui.seek.setAttribute(SEEKING_ATTR, true);
	audio.muted = true; // mute the thing while seeking so that it doesn't squeak
}
function onSeekChange() { // user-initiated event
	if (!initialized()) return;

	const value = ui.seek.value;
	audio.currentTime = value;
	seek(value);
}
function onSeekMouseUp() {
	ui.seek.removeAttribute(SEEKING_ATTR);
	audio.muted = false;
}

// UI STUFF
function title(title) {
	ui.title.innerHTML = title || Native.FS.readablePath(State.get(State.Key.TRACK));
	ui.title.setAttribute('title', ui.title.textContent);
	Native.Window.title(ui.title.textContent);
}
function albumArtist(album, artist) {
	album = album || Native.FS.readablePath(State.get(State.Key.CURRENT_DIR)); // default to current dir for no-album-in-metadata case

	ui.albumArtist.innerHTML = `<strong>${album}</strong> ${artist ? '| ' + artist : ''}`;
	ui.albumArtist.setAttribute('title', ui.albumArtist.textContent);
}
function artwork(art) {
	if (art) ui.artwork.setAttribute('src', art);
	ui.artwork.classList.toggle('hidden', !art);
}
function readableTime(seconds) {
	const ss = parseInt(seconds % 60).toString().padStart(2, '0');
	const mm = parseInt((seconds / 60) % 60).toString().padStart(2, '0');
	const hh = parseInt((seconds / 60 / 60)).toString().padStart(2, '0');

	const hhMax = parseInt((ui.seek.max / 60 / 60)).toString().padStart(2, '0');

	return hhMax == '00' ? `${mm}:${ss}` : `${hh}:${mm}:${ss}`;
}
function loadingIndicator(force) {
	ui.albumArtist.classList.toggle('blur', force);
	ui.duration.classList.toggle('blur', force);
	if (force) ui.artwork.classList.add('hidden');
}
function progressBar(force) {
	// must take absolute value because the seek value can arbitrarily change (for example, manual seeking or when changing tracks)
	if (!force && Math.abs(ui.seek.value - lastProgressBarUpdate) < 1) return;
	Native.Window.progressBar(audio.paused ? 'paused' : 'normal', ui.seek.value / ui.seek.max * 100);
	lastProgressBarUpdate = ui.seek.value;
}
