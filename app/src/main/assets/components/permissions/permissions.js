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
			<p l1n0>Allow <strong>Music and audio</strong> access</p>
			<button class="ic-btn pressable ic-unlock" onclick="${this.handle}.request()" aria-label="grant music and audio permission" l10n></button>
		`);
	}
}

customElements.define('music-permissions', MusicPermissions);