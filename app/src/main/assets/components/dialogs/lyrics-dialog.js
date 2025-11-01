class LyricsDialog extends HTMLElementBase {

	#TARGET = EventBus.Target.LYRICS;

	#syncedLyrics;
	#activeVerse;

	connectedCallback() {
		this.#render();
		EventBus.subscribe((event) => this.#handler(event));
	}

	open() {
		this.setAttribute('open', '');
		this.querySelector('.active')?.scrollIntoView({ block: 'center' });
	}
	close() {
		this.removeAttribute('open');
	}

	#handler(event) {
		if (event.target == this.#TARGET) return;

		when(event.type)
			.is(EventBus.Type.MODE_CHANGE, () => state.mode == state.Mode.LYRICS ? this.open() : this.close())
			.is([EventBus.Type.RESTORE_STATE, EventBus.Type.METADATA_UPDATE], () => {
				this.#renderLyrics();
			})
			.is([EventBus.Type.SEEK_TICK, EventBus.Type.SEEK_UPDATE], () => {
				if (!this.#syncedLyrics) return;
				this.#updateActiveVerse();
			})
	}

	// RENDERING
	#render() {
		super.render(`
			<i class="dialog-header ic-lyrics"></i>
			<p id="lyrics" class="dialog-content"></p>
		`);
	}
	#renderLyrics() {
		if (Array.isArray(state.track.lyrics)) {
			this.#syncedLyrics = state.track.lyrics;
			this.lyrics.innerHTML = this.#syncedLyrics.map(v => `<verse>${v.text}</verse>`).join('');

		} else {
			this.lyrics.innerHTML = state.track.lyrics || '';
		}
	}

	#updateActiveVerse() {
		const within = (v) => v?.startTime <= state.track.seek && v?.endTime >= state.track.seek;

		if (within(this.#activeVerse)) return;

		const prevIndex = this.#syncedLyrics.indexOf(this.#activeVerse);
		this.lyrics.children[prevIndex]?.classList?.remove('active');

		const activeIndex = this.#syncedLyrics.findIndex(v => within(v));
		this.#activeVerse = this.#syncedLyrics[activeIndex];

		this.lyrics.children[activeIndex]?.classList.add('active');
		this.lyrics.children[activeIndex]?.scrollIntoView({ behavior: 'smooth', block: 'center' });
	}
}

customElements.define('lyrics-dialog', LyricsDialog);
