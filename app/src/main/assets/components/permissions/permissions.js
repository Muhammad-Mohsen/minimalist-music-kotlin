class MusicPermissions extends HTMLElementBase {

	SELF = EventBus.Target.PERMISSIONS;

	connectedCallback() {
		this.#render();
	}

	request() {
		EventBus.dispatch({ type: EventBus.Type.PERMISSION_REQUEST, target: EventBus.Target.PERMISSION_UI });
	}

	#render() {
		super.render(`
			<p l10n>Allow <strong>Music and audio</strong> access</p>
			<button class="ic-btn ic-unlock main-character" onclick="${this.handle}.request()" aria-label="grant music and audio permission"></button>
		`);
	}
}

customElements.define('music-permissions', MusicPermissions);
