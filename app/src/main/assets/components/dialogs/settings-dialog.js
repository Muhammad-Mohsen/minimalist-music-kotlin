class SettingsDialog extends HTMLElementBase {

	#TARGET = EventBus.Target.SETTINGS;

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
			.is(EventBus.Type.RESTORE_STATE, () => this.#restore())
			.is(EventBus.Type.MODE_CHANGE, () => state.mode == state.Mode.SETTINGS ? this.open() : this.close())
			.is(EventBus.Type.SLEEP_TIMER_TICK, () => {
				this.countdown.value = event.data.tick;
				this.countdownValue.innerHTML = readableTime(event.data.tick);
			})
			.is(EventBus.Type.SLEEP_TIMER_FINISH, () => {
				this.sleepTimerToggle.removeAttribute('checked');
				this.countdown.value = 0;
				this.countdownValue.innerHTML = '';
			})
			.is(EventBus.Type.TOGGLE_SHUFFLE, () => {
				this.shuffleButton.classList.toggle('selected', state.settings.shuffle);
			})
			.is(EventBus.Type.TOGGLE_REPEAT, () => {
				this.repeatIcon.className = RepeatIcons[state.settings.repeat];
				this.repeatButton.classList.toggle('selected', state.settings.repeat > 0);
			})
	}

	// HANDLERS
	toggleTheme(target, theme) {
		this.lightThemeButton.classList.remove('selected');
		this.darkThemeButton.classList.remove('selected');

		state.settings.theme = theme;
		target.classList.add('selected');

		EventBus.dispatch({ type: EventBus.Type.THEME_CHANGE, target: this.#TARGET, data: { value: state.settings.theme } });
	}

	onSleepTimerChange() {
		state.settings.sleepTimer = this.sleepTimer.value;
		this.sleepTimerValue.innerHTML = readableTime(state.settings.sleepTimer, 'hh:mm');
		EventBus.dispatch({ type: EventBus.Type.SLEEP_TIMER_CHANGE, target: this.#TARGET, data: { value: state.settings.sleepTimer } });
	}
	toggleSleepTimer() {
		this.sleepTimerToggle.toggleAttribute('checked');

		const active = this.sleepTimerToggle.hasAttribute('checked');
		EventBus.dispatch({ type: EventBus.Type.SLEEP_TIMER_TOGGLE, target: this.#TARGET, data: { value: active } });

		if (!active) {
			this.countdown.value = 0;
			this.countdownValue.innerHTML = '';
		}
	}

	onPlaybackSpeedChange() {
		state.settings.playbackSpeed = this.playbackSpeed.value;
		this.playbackSpeedValue.innerHTML = this.playbackSpeed.value + 'x';
		EventBus.dispatch({ type: EventBus.Type.PLAYBACK_SPEED_CHANGE, target: this.#TARGET, data: { value: state.settings.playbackSpeed } });
	}

	onSeekJumpChange() {
		state.settings.seekJump = parseInt(this.seekJump.value);
		this.seekJumpValue.innerHTML = (this.seekJump.value / 1000) + ' seconds';
		EventBus.dispatch({ type: EventBus.Type.SEEK_JUMP_CHANGE, target: this.#TARGET, data: { value: state.settings.seekJump } });
	}

	onSortByChange() {
		state.settings.sortBy = this.sortBy.value;
		EventBus.dispatch({ type: EventBus.Type.SORT_BY_CHANGE, target: this.#TARGET, data: { value: state.settings.sortBy } });
	}

	toggleShuffle() {
		state.settings.shuffle = !state.settings.shuffle;
		EventBus.dispatch({ type: EventBus.Type.TOGGLE_SHUFFLE, target: this.#TARGET, data: { value: state.settings.shuffle } });

		this.shuffleButton.classList.toggle('selected', state.settings.shuffle);
	}
	toggleRepeat() {
		state.settings.repeat = (state.settings.repeat + 1) % 3;
		EventBus.dispatch({ type: EventBus.Type.TOGGLE_REPEAT, target: this.#TARGET, data: { value: state.settings.repeat } });

		this.repeatIcon.className = RepeatIcons[state.settings.repeat];
		this.repeatButton.classList.toggle('selected', state.settings.repeat > 0);
	}
	showEqualizer() {
		state.mode = state.Mode.EQUALIZER;
		// note the target value, which "cheats" which components are notified!
		EventBus.dispatch({ type: EventBus.Type.MODE_CHANGE, target: EventBus.Target.CONTROLS, data: { mode: state.mode } });
	}

	onSecondaryControlsChange(target, index) {
		state.settings.secondaryControls[index] = target.value;
		EventBus.dispatch({ type: EventBus.Type.SECONDARY_CONTROLS_CHANGE, target: this.#TARGET, data: { value: state.settings.secondaryControls.join(';') } });
	}

	showPrivacyPolicy() {
		EventBus.dispatch({ type: EventBus.Type.PRIVACY_POLICY, target: EventBus.Target.CONTROLS });
	}

	#restore() {
		this.darkThemeButton.classList.toggle('selected', state.settings.theme != state.Theme.LIGHT);
		this.lightThemeButton.classList.toggle('selected', state.settings.theme == state.Theme.LIGHT);

		this.sleepTimer.value = state.settings.sleepTimer;
		this.sleepTimerValue.innerHTML = readableTime(state.settings.sleepTimer, 'hh:mm');

		this.playbackSpeed.value = state.settings.playbackSpeed;
		this.playbackSpeedValue.innerHTML = state.settings.playbackSpeed + 'x';

		this.seekJump.value = state.settings.seekJump;
		this.seekJumpValue.innerHTML = `${state.settings.seekJump / 1000} ${this.translate('Seconds') }`;

		this.sortBy.value = state.settings.sortBy;

		this.shuffleButton.classList.toggle('selected', state.settings.shuffle);

		this.repeatIcon.className = RepeatIcons[state.settings.repeat];
		this.repeatButton.classList.toggle('selected', state.settings.repeat > 0);

		this.#renderSecondaryControlsCustomization();
	}

	// RENDERING
	#render() {
		super.render(`
			<i class="ic-header ic-more"></i>

			<div class="flex-row">
				<button id="light-theme-button" class="settings-btn" onclick="${this.handle}.toggleTheme(this, 'light')">
					<i class="ic-sun"></i>
					<span l10n>Light</span>
				</button>
				<separator></separator>
				<button id="dark-theme-button" class="settings-btn selected" onclick="${this.handle}.toggleTheme(this, 'dark')">
					<i class="ic-moon"></i>
					<span l10n>Dark</span>
				</button>
			</div>

			<div id="secondary-controls-customization"></div>

			<div class="range-row">
				<!-- min: 10m - max: 3h - step: 5m converted to millis -->
				<input type="range" id="countdown" min="600000" max="10800000" value="0">
				<input type="range" id="sleep-timer" min="600000" max="10800000" step="300000" oninput="${this.handle}.onSleepTimerChange()">

				<div class="label">
					<label for="sleep-timer" l10n>Sleep Timer</label>
					<span class="subscript">
						<strong id="countdown-value"></strong>
						<output for="sleep-timer" id="sleep-timer-value"></output>
					</span>
				</div>

				<separator></separator>
				<button id="sleep-timer-toggle" class="ic-btn toggle-btn ic-sleep-timer" onclick="${this.handle}.toggleSleepTimer()"></button>
			</div>

			<div class="range-row">
				<input type="range" id="playback-speed" min=".25" max="2.5" step=".25" oninput="${this.handle}.onPlaybackSpeedChange()">

				<div class="label">
					<label for="playback-speed" l10n>Playback Speed</label>
					<output class="subscript" for="playback-speed" id="playback-speed-value"></output>
				</div>
				<i class="ic-playback-speed"></i>
			</div>

			<div class="range-row">
				<input type="range" id="seek-jump" min="10000" max="300000" step="5000" oninput="${this.handle}.onSeekJumpChange()">

				<div class="label">
					<label for="seek-jump" l10n>Seek Jump</label>
					<output class="subscript" for="seek-jump" id="seek-jump-value"></output>
				</div>
				<i class="ic-seek-jump"></i>
			</div>

			<div class="range-row select">
				<select id="sort-by" onchange="${this.handle}.onSortByChange()">
					<option value="az" l10n>Name (A to Z)</option>
					<option value="za" l10n>Name (Z to A)</option>
					<option value="newest" l10n>Date (Newest First)</option>
					<option value="oldest" l10n>Date (Oldest First)</option>
				</select>
				<div class="label">
					<label for="sort-by" l10n>Sort By</label>
				</div>
				<i class="ic-sort"></i>
			</div>

			<div class="flex-row">
				<button id="shuffle-button" class="settings-btn" onclick="${this.handle}.toggleShuffle()">
					<i class="ic-shuffle"></i>
					<span l10n>Shuffle</span>
				</button>
				<separator></separator>
				<button id="repeat-button" class="settings-btn" onclick="${this.handle}.toggleRepeat()">
					<i id="repeat-icon" class="ic-repeat"></i>
					<span l10n>Repeat</span>
				</button>
				<separator></separator>
				<button class="settings-btn" onclick="${this.handle}.showEqualizer()">
					<i class="ic-equalizer"></i>
					<span l10n>Equalizer</span>
				</button>
			</div>

			<button class="pp" l10n onclick="${this.handle}.showPrivacyPolicy()">Privacy Policy</button>
		`);
	}

	#renderSecondaryControlsCustomization() {
		this.secondaryControlsCustomization.innerHTML = `
			<label>${this.translate('UI Customization')}</label>
			<div class="flex-row" style="margin-inline: -6px;">
				${state.settings.secondaryControls.map((val, i) => {
					return `
						<select id="ui-custom-${i}" onchange="${this.handle}.onSecondaryControlsChange(this, ${i})">
							<button><selectedcontent></selectedcontent></button>

							${Object.keys(SecondaryControlOptions).map(key => {
								return `
								<option value="${key}" ${val == key ? 'selected' : ''}>
									<i class="${SecondaryControlOptions[key].icon}"></i>
									<span l10n>${this.translate(SecondaryControlOptions[key].label)}</span>
								</option>
							`;
							}).join('')}

						</select>
					`;
				}).join('<separator></separator>')}
			</div>
		`;
	}
}

customElements.define('settings-dialog', SettingsDialog);
