class LyricsDialog extends HTMLElementBase {

	#TARGET = EventBus.Target.LYRICS;

	connectedCallback() {
		this.#render();
		EventBus.subscribe((event) => this.#handler(event));
	}

	open() {
		this.setAttribute('open', '');
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
	}

	// RENDERING
	#render() {
		super.render(`
			<i class="ic-header ic-lyrics"></i>
			<p id="lyrics"></p>
		`);
	}
	#renderLyrics() {
		this.lyrics.innerHTML = state.track.lyrics || '';
	}
}

customElements.define('lyrics-dialog', LyricsDialog);
