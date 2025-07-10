class EqualizerDialog extends HTMLElementBase {

	#TARGET = EventBus.Target.EQUALIZER;

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
			.is(EventBus.Type.MODE_CHANGE, () => state.mode == state.Mode.EQUALIZER ? this.open() : this.close())
			.is(EventBus.Type.EQUALIZER_INFO, () => {
				this.#renderPresets(event.data.presets, event.data.currentPreset);
				this.#renderBands(event.data.bands);
			})
	}

	// HANDLERS
	onPresetChange(preset) {
		// TODO update state
		EventBus.dispatch({ type: EventBus.Type.EQUALIZER_PRESET, target: this.#TARGET, data: { value: preset } });
	}
	onBandChange(band, value) {
		EventBus.dispatch({ type: EventBus.Type.EQUALIZER_BAND_CHANGE, target: this.#TARGET, data: { value: preset } });
	}

	// RENDERING
	#render() {
		super.render(`
			<i class="ic-header ic-equalizer"></i>

			<ul id="bands"></ul>
			<ul id="presets" class="flex-row"></ul>
		`);
	}
	#renderPresets(presets, currentPreset) {
		this.presets.innerHTML = presets.map(p => `
			<label>${p.name}<input type="radio" name="presets" ${p.id == currentPreset ? 'checked' : ''} oninput="${this.handle}.onPresetChange(${p.id})">
			</label>
		`).join('<separator></separator>');
	}
	#renderBands(bands) {
		this.bands.innerHTML = bands.map(b => {
			return `
				<div class="eq-band">
					<label>${this.#bandLabel(b.centerFrequency)}</label>
					<input type="range" min="${b.low}" max="${b.high}" value="${b.level}" oninput="${this.handle}.onBandChange(${b.id}, this.value)">
				</div>
			`;
		}).join('');
	}
	#bandLabel(frequency) {
		frequency = frequency / 1000;
		return frequency > 1000 ? (frequency / 1000 + 'k') : frequency;
	}
}

customElements.define('equalizer-dialog', EqualizerDialog);
