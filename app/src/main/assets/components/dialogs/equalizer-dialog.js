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
		// TODO update state?
		EventBus.dispatch({ type: EventBus.Type.EQUALIZER_PRESET_CHANGE, target: this.#TARGET, data: { value: preset } });
	}
	onBandChange(band, value) {
		// TODO update state?
		EventBus.dispatch({ type: EventBus.Type.EQUALIZER_BAND_CHANGE, target: this.#TARGET, data: { band, value } });
	}

	// RENDERING
	#render() {
		super.render(`
			<i class="ic-header ic-equalizer"></i>

			<ul id="bands"></ul>
			<ul id="presets" class="flex-row"></ul>
		`);
	}
	#renderBands(bands) {
		if (this.bands.childElementCount) return this.#updateBands(bands);

		this.bands.innerHTML = bands.map(b => {
			return `
				<div class="eq-band">
					<label>${this.#bandLabel(b.centerFrequency)}</label>
					<input type="range" min="${b.low}" max="${b.high}" step="10" value="${b.level}" oninput="${this.handle}.onBandChange(${b.id}, this.value)">
				</div>
			`;
		}).join('');
	}
	#bandLabel(frequency) {
		frequency = frequency / 1000;
		return frequency > 1000 ? (frequency / 1000 + 'k') : frequency;
	}
	#renderPresets(presets, currentPreset) {
		if (this.presets.childElementCount) return this.#updatePresets(currentPreset);

		this.presets.innerHTML = presets.map(p => `
			<label>${p.name}<input type="radio" name="presets" value="${p.id}" ${p.id == currentPreset ? 'checked' : ''} oninput="${this.handle}.onPresetChange(${p.id})">
			</label>
		`).join('<separator></separator>');
	}

	#updateBands(bands) {
		const duration = 150;

		const inputs = Array.from(this.querySelectorAll('input[type="range"]'));
		const startValues = inputs.map(input => parseInt(input.value));

		let startTime;
		const animate = (currentTime) => {
			if (!startTime) startTime = currentTime;
			const dt = (currentTime - startTime) / duration;

			if (dt < 1) {
				inputs.forEach((input, i) => input.value = startValues[i] + (bands[i].level - parseInt(startValues[i])) * dt);

				requestAnimationFrame(animate);

			} else {
				inputs.forEach((input, i) => input.value = bands[i].level);
			}
		}

		requestAnimationFrame(animate);
	}
	#updatePresets(currentPreset) {
		this.querySelector(`input[name="presets"][value="${currentPreset}"]`).click();
	}

}

customElements.define('equalizer-dialog', EqualizerDialog);
