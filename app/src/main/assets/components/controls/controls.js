class MusicControls extends HTMLElementBase {

	#TARGET = EventBus.Target.CONTROLS;

	#albumArt;

	customizableButtons = {
		RW: `<button id="rw-button" class="ic-btn ic-rw" onclick="${this}.rewind()" aria-label="Rewind"></button>`,
		PREV: `<button id="previous-button" class="ic-btn ic-prev" onclick="${this}.playPrev();" aria-label="Previous"></button>`,
		NEXT: `<button id="next-button" class="ic-btn ic-next" onclick="${this}.playNext();" aria-label="Next"></button>`,
		FF: `<button id="ff-button" class="ic-btn ic-ff" onclick="${this}.fastForward()" aria-label="Fast Forward"></button>`,
		SHUFFLE: `<button id="shuffle-button" class="ic-btn ic-shuffle" onclick="${this}.toggleShuffle(this)" aria-label="Toggle Shuffle"></button>`,
		REPEAT: `<button id="repeat-button" class="ic-btn ic-repeat" onclick="${this}.toggleRepeat(this)" aria-label="Repeat Mode"></button>`,
		SEARCH: `<button id="search-button" class="ic-btn ic-search" onclick="${this}.toggleSearch(this);" aria-label="Search"></button>`,
		CHAPTERS: `<button id="chapters-button" class="ic-btn ic-chapters" onclick="${this}.toggleChapters(this)" aria-label="Chapters"></button>`,
		LYRICS: `<button id="lyrics-button" class="ic-btn ic-lyrics" onclick="${this}.toggleLyrics(this)" aria-label="Lyrics"></button>`,
		EQUALIZER: `<button id="equalizer-button" class="ic-btn ic-equalizer" onclick="${this}.toggleEqualizer(this)" aria-label="Equalizer"></button>`,
		ALBUM_ART: `<button id="album-art-button" class="ic-btn ic-album-art passive-toggle" onclick="${this}.toggleAlbumArt(this)" aria-label="Album Art"></button>`,
	}

	connectedCallback() {
		this.#render();
		this.albumArt = document.querySelector('#album-art'); // outside of the component so it can appear below the dialogs
		EventBus.subscribe((event) => this.#handler(event));
	}

	// EVENT BUS
	#handler(event) {
		if (event.target == this.#TARGET) return;

		when(event.type)
			.is(EventBus.Type.MODE_CHANGE, () => this.querySelector('.secondary-controls [checked]')?.removeAttribute('checked'))
			.is(EventBus.Type.RESTORE_STATE, () => {
				this.#updateMetadata();
				this.#renderSecondaryControls();
				this.#restoreShuffle();
				this.#restoreRepeat();
			})
			.is(EventBus.Type.METADATA_UPDATE, () => this.#updateMetadata())
			.is([EventBus.Type.PLAY, EventBus.Type.PLAY_TRACK, EventBus.Type.QUEUE_PLAY_SELECTED], () => this.playPause(true, 'suppress'))
			.is(EventBus.Type.PAUSE, () => this.playPause(false, 'suppress'))
			.is(EventBus.Type.PLAY_PAUSE, () => this.playPause())
			.is(EventBus.Type.DURATION_UPDATE, () => {
				state.track.duration = event.data.duration;
				this.#updateSeekUI();
				this.#updateChapters();
			})
			.is(EventBus.Type.SEEK_TICK, () => {
				state.track.seek = event.data.seek;
				this.#updateSeekUI();
			})
			.is(EventBus.Type.SLEEP_TIMER_FINISH, () => this.playPause(false))
			.is(EventBus.Type.SECONDARY_CONTROLS_CHANGE, () => this.#renderSecondaryControls())
			.is(EventBus.Type.TOGGLE_SHUFFLE, () => this.#restoreShuffle())
			.is(EventBus.Type.TOGGLE_REPEAT, () => this.#restoreRepeat())
	}

	// UI HANDLERS
	playPause(play, suppress) {
		play = play != undefined ? play : !state.isPlaying;

		// dispatching is suppressed if this was called in response to another event (for example, PLAY_NEXT, or pausing from the notification)
		if (!suppress) EventBus.dispatch({ type: play ? EventBus.Type.PLAY : EventBus.Type.PAUSE, target: this.#TARGET });
		if (play == state.isPlaying) return; // already in the correct state

		state.isPlaying = play;
		this.#togglePlayPauseUI(play);
	}
	playNext() {
		this.playPause(true, 'suppress');
		EventBus.dispatch({ type: EventBus.Type.PLAY_NEXT, target: this.#TARGET });
	}
	playPrev() {
		this.playPause(true, 'suppress');
		EventBus.dispatch({ type: EventBus.Type.PLAY_PREV, target: this.#TARGET });
	}
	rewind() {
		state.track.seek = Math.max(0, state.track.seek - state.settings.seekJump);
		this.#updateSeekUI();
		EventBus.dispatch({ type: EventBus.Type.SEEK_UPDATE, target: this.#TARGET, data: { seek: state.track.seek } });
	}
	fastForward() {
		state.track.seek = Math.min(state.track.duration, state.track.seek + state.settings.seekJump);
		this.#updateSeekUI();
		EventBus.dispatch({ type: EventBus.Type.SEEK_UPDATE, target: this.#TARGET, data: { seek: state.track.seek } });
	}

	onSeekChange(target) {
		state.track.seek = target.value;
		this.#updateSeekUI();
		EventBus.dispatch({ type: EventBus.Type.SEEK_UPDATE, target: this.#TARGET, data: { seek: state.track.seek } });
	}

	toggleSearch(target) {
		target.toggleAttribute('checked');

		state.mode = when(state.mode)
			.is(state.Mode.NORMAL, () => state.Mode.SEARCH)
			.is(state.Mode.SEARCH, () => state.Mode.NORMAL)
			.is(state.Mode.SELECT, () => state.Mode.SEARCH_SELECT)
			.is(state.Mode.SEARCH_SELECT, () => state.Mode.SELECT)
			.val();

		if (![state.Mode.SEARCH, state.Mode.SEARCH_SELECT].includes(state.mode)) state.query = '';
		EventBus.dispatch({ type: EventBus.Type.MODE_CHANGE, target: this.#TARGET, data: { mode: state.mode } });
	}

	toggleSettings() {
		state.mode = state.mode == state.Mode.SETTINGS ? state.Mode.NORMAL : state.Mode.SETTINGS;

		this.moreButton.toggleAttribute('checked', state.mode == state.Mode.SETTINGS);
		EventBus.dispatch({ type: EventBus.Type.MODE_CHANGE, target: this.#TARGET, data: { mode: state.mode } });
	}
	toggleChapters(target) {
		state.mode = state.mode == state.Mode.CHAPTERS ? state.Mode.NORMAL : state.Mode.CHAPTERS;

		target.toggleAttribute('checked', state.mode == state.Mode.CHAPTERS);
		EventBus.dispatch({ type: EventBus.Type.MODE_CHANGE, target: this.#TARGET, data: { mode: state.mode } });
	}
	toggleLyrics(target) {
		state.mode = state.mode == state.Mode.LYRICS ? state.Mode.NORMAL : state.Mode.LYRICS;

		target.toggleAttribute('checked', state.mode == state.Mode.LYRICS);
		EventBus.dispatch({ type: EventBus.Type.MODE_CHANGE, target: this.#TARGET, data: { mode: state.mode } });
	}
	toggleAlbumArt(target) {
		state.settings.albumArt = !state.settings.albumArt;

		target.toggleAttribute('checked', !state.settings.albumArt);
		this.#updateAlbumArt();
		EventBus.dispatch({ type: EventBus.Type.TOGGLE_ALBUM_ART, target: this.#TARGET, data: { value: state.settings.albumArt } });
	}
	toggleEqualizer(target) {
		state.mode = state.mode == state.Mode.EQUALIZER ? state.Mode.NORMAL : state.Mode.EQUALIZER;

		target.toggleAttribute('checked', state.mode == state.Mode.EQUALIZER);
		EventBus.dispatch({ type: EventBus.Type.MODE_CHANGE, target: this.#TARGET, data: { mode: state.mode } });
	}
	toggleShuffle(target) {
		state.settings.shuffle = !state.settings.shuffle;
		EventBus.dispatch({ type: EventBus.Type.TOGGLE_SHUFFLE, target: this.#TARGET, data: { value: state.settings.shuffle } });

		target.classList.toggle('selected', state.settings.shuffle);
	}
	toggleRepeat(target) {
		state.settings.repeat = (state.settings.repeat + 1) % 3;
		EventBus.dispatch({ type: EventBus.Type.TOGGLE_REPEAT, target: this.#TARGET, data: { value: state.settings.repeat } });

		target.className = RepeatIcons[state.settings.repeat] + ' ic-btn';
		target.classList.toggle('selected', state.settings.repeat > 0);
	}

	#restoreShuffle() {
		this.querySelector('#shuffle-button')?.classList?.toggle('selected', state.settings.shuffle);
	}
	#restoreRepeat() {
		const repeat = this.querySelector('#repeat-button');
		if (!repeat) return;
		repeat.className = RepeatIcons[state.settings.repeat] + ' ic-btn';
		repeat.classList.toggle('selected', state.settings.repeat > 0);
	}

	// RENDERING
	#render() {
		super.render(`
			<div class="main-controls">
				<input type="range" id="seek-range" value="0" oninput="${this}.onSeekChange(this)">

				<button id="play-pause-button" class="ic-btn main-character" onclick="${this}.playPause()" aria-label="Play/Pause">
					<svg  width="180" height="16" viewBox="0 0 180 16" fill="none" xmlns="http://www.w3.org/2000/svg">
						<path d="M0 8H85V1H94V5.5M180 8H94V15H85V10.5" id="pause-path" style="stroke-dashoffset: 107;" stroke="var(--foreground)"/>
						<path d="M0 8H82.9998V15L93.5 9.34614M180 8H96L82.9998 1V5.5" id="play-path" style="stroke-dashoffset: 0;" stroke="var(--foreground)"/>
					</svg>
				</button>

				<div class="seek-text-container">
					<output id="seek-current"></output>
					<output id="seek-duration">&nbsp</output>
				</div>

				<ul id="chapters"></ul>
				<h1 id="track-name">${this.translate('Hi')},</h1>
				<h2 id="track-album-artist">${this.translate('Welcome to Minimalist Music Player')}</h2>
			</div>

			<div class="secondary-controls">
				<div id="customizable-controls"></div>
				<button id="more-button" class="ic-btn ic-more" onclick="${this}.toggleSettings()" aria-label="More"></button>
			</div>
		`);
	}

	#togglePlayPauseUI(play) {
		setTimeout(() => this.pausePath.style.strokeDashoffset = parseInt(this.pausePath.style.strokeDashoffset) + 107, play ? 300 : 0);
		setTimeout(() => this.playPath.style.strokeDashoffset = parseInt(this.playPath.style.strokeDashoffset) + 104, play ? 0 : 300);
	}

	#updateMetadata() {
		this.#updateSeekUI();

		// replace name animation
		this.#updateTrackText(this.trackName, 300, state.track.name);

		// replace album | artist animation
		this.#updateTrackText(this.trackAlbumArtist, 400, `<strong>${state.track.album}</strong> ${state.track.artist ? ' | ' : ''} ${state.track.artist}`);

		this.#updateChapters();
		this.#updateAlbumArt();

		state.notifyReady();
	}
	#updateSeekUI() {
		// max needs to be set before the value
		if (this.seekRange.max != state.track.duration) {
			this.seekDuration.innerHTML = readableTime(state.track.duration);
			this.seekRange.max = state.track.duration;
		}
		// only show the seek if duration is set
		this.seekCurrent.innerHTML = state.track.duration ? readableTime(state.track.seek) : '';
		this.seekRange.value = state.track.seek;
	}
	#updateTrackText(elem, delay, val) {
		if (!state.isReady) {
			elem.finishAnimations(); // don't run the initial animation
			return elem.innerHTML = val;
		}

		if (elem.innerHTML == val) return; // don't run the animation if the value is the same

		elem.replayAnimations();
		setTimeout(() => elem.innerHTML = val, delay);
	}
	#updateAlbumArt() {
		this.albumArt.style.opacity = state.settings.albumArt ? '' : 0;
		if (this.#albumArt == state.track.albumArt) return;
		this.#albumArt = state.track.albumArt;

		this.albumArt.classList.add('hidden');
		if (!state.track.albumArt) return;

		setTimeout(() => {
			this.albumArt.setAttribute('src', 'data:image/png;base64, ' + state.track.albumArt.replace('file:///android_asset/', ''));
			this.albumArt.classList.remove('hidden');
		}, 300);
	}
	#updateChapters() {
		this.chapters.innerHTML = state.track.chapters
			?.slice(1) // drop the first chapter (we don't care about 00:00)
			?.map(c => `<li style="inset-inline-start:${c.startTime / state.track.duration * this.chapters.clientWidth}px"></li>`)
			?.join('') || '';
	}

	#renderSecondaryControls() {
		this.customizableControls.innerHTML = state.settings.secondaryControls.map(key => this.customizableButtons[key]).join('');
	}
}

customElements.define('music-controls', MusicControls);
