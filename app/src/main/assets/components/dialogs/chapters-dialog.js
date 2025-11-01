class ChaptersDialog extends HTMLElementBase {

	#TARGET = EventBus.Target.CHAPTERS;

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
			.is(EventBus.Type.MODE_CHANGE, () => state.mode == state.Mode.CHAPTERS ? this.open() : this.close())
			.is([EventBus.Type.RESTORE_STATE, EventBus.Type.METADATA_UPDATE], () => {
				this.#renderChapters();
			})
	}

	// HANDLERS
	onChapterClick(timestamp) {
		EventBus.dispatch({ type: EventBus.Type.SEEK_UPDATE, target: this.#TARGET, data: { seek: timestamp } });
	}

	// RENDERING
	#render() {
		super.render(`
			<i class="dialog-header ic-chapters"></i>
			<ul id="chapters" class="dialog-content"></ul>
		`);
	}
	#renderChapters() {
		this.chapters.innerHTML = state.track.chapters?.map(c => `
			<button onclick="${this.handle}.onChapterClick(${c.startTime})">
				<div class="title">${c.title}</div>
				<div class="timestamp">${readableTime(c.startTime)}</div>
			</button>
		`)?.join('') || '';
	}
}

customElements.define('chapters-dialog', ChaptersDialog);
