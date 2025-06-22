class MusicControls extends HTMLElementBase {

	#TARGET = EventBus.Target.CONTROLS;

	connectedCallback() {
		this.#render();
		EventBus.subscribe((event) => this.#handler(event));
	}

	// EVENT BUS
	#handler(event) {
		if (event.target == this.#TARGET) return;

		when(event.type)
			.is(EventBus.Type.MODE_CHANGE, () => {
				this.searchButton.classList.remove('ic-close'); // TODO fugly shit
				this.searchButton.classList.add('ic-search');
			})
			.is([EventBus.Type.RESTORE_STATE, EventBus.Type.METADATA_UPDATE], () => this.#updateMetadata())
			.is(EventBus.Type.PLAY, () => playPause(true, 'suppress'))
			.is(EventBus.Type.PAUSE, () => playPause(false, 'suppress'))
			.is(EventBus.Type.PLAY_PAUSE, () => playPause())
	}

	#updateMetadata() {
		this.#updateSeekUI();

		this.trackName.innerHTML = state.track.name;
		const dir = state.currentDir.split(Path.SEPARATOR).pop();
		this.trackAlbumArtist.innerHTML = `<strong>${state.track.album || dir}</strong> ${state.track.artist ? ' | ' : ''} ${state.track.artist}`;

		this.chapters.innerHTML = ''; // TODO

		if (state.track.art) this.art.setAttribute('src', state.track.art);
		this.art.classList.toggle('hidden', !state.track.art);
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

	onSeekTouchStart() {
		EventBus.dispatch({ type: EventBus.Type.SEEKING, target: this.#TARGET, data: { isSeeking: true } });
	}
	onSeekChange(target) {
		state.track.seek = target.value;
		this.#updateSeekUI();
		EventBus.dispatch({ type: EventBus.Type.SEEK_UPDATE_REQUEST, target: this.#TARGET, data: { seek: state.track.seek } });
	}
	onSeekTouchEnd() {
		EventBus.dispatch({ type: EventBus.Type.SEEKING, target: this.#TARGET, data: { isSeeking: false } });
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

	// RENDERING
	#render() {
		super.render(`
			<img alt="Album Art" id="art">
			<div class="main-controls">
				<input type="range" id="seek-range" value="0" ontouchstart="${this.handle}.onSeekTouchStart()" ontouchend="${this.handle}.onSeekTouchEnd()" oninput="${this.handle}.onSeekChange(this)">

				<button id="play-pause-button" class="ic-btn" aria-label="play/pause" onclick="${this.handle}.playPause()">
					<svg  width="180" height="15" fill="none" xmlns="http://www.w3.org/2000/svg">
						<path id="pause-path" style="stroke-dashoffset: 75;" d="M94 15V7.5H180M85 0V7.5H0" stroke="var(--foreground)"/>
						<path id="play-path" style="stroke-dashoffset: 0;" d="M83.0469 1L95.9531 7.5H180M83.0469 15V7.5H0" stroke="var(--foreground)"/>
					</svg>
				</button>

				<div class="seek-text">
					<output id="seek-current"></output>
					<output id="seek-duration">&nbsp</output>
				</div>

				<ul id="chapters"></ul>
				<h1 id="track-name">Hi,</h1>
				<h2 id="track-album-artist">Welcome to Minimalist Music</h2>
			</div>

			<div class="secondary-controls">
				<button id="search-button" class="ic-btn ic-search" onclick="${this.handle}.toggleSearch();" aria-label="search"></button>
				<button id="chapters-button" class="ic-btn ic-chapters" aria-label="chapters"></button>
				<button id="previous-button" class="ic-btn ic-prev" onclick="${this.handle}.playPrev();" aria-label="previous"></button>
				<button id="next-button" class="ic-btn ic-next" onclick="${this.handle}.playNext();" aria-label="next"></button>
				<button id="lyrics-button" class="ic-btn ic-lyrics" aria-label="lyrics"></button>
				<button id="more-button" class="ic-btn ic-more" aria-label="more"></button>
			</div>
		`);
	}

	#togglePlayPauseUI(play) {
		setTimeout(() => this.pausePath.style.strokeDashoffset = parseInt(this.pausePath.style.strokeDashoffset) + 75, play ? 200 : 0);
		setTimeout(() => this.playPath.style.strokeDashoffset = parseInt(this.playPath.style.strokeDashoffset) + 80, play ? 0 : 200);
	}
	#updateSeekUI() {
		this.seekCurrent.innerHTML = readableTime(state.track.seek);
		this.seekRange.value = state.track.seek;

		if (this.seekRange.max == state.track.duration) return;
		this.seekDuration.innerHTML = readableTime(state.track.duration);
		this.seekRange.max = state.track.duration;
	}
}

customElements.define('music-controls', MusicControls);
