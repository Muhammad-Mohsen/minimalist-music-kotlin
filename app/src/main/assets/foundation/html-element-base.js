class HTMLElementBase extends HTMLElement {
	static salt = 0;
	handle;

	constructor() {
		super();

		this.handle = `${this.constructor.name}_${HTMLElementBase.salt++}_${Date.now()}`;
		window[this.handle] = this;

		window.addEventListener('message', (event) => this.onMessage(event));
	}

	render(template) {
		this.innerHTML = template.replace(/\t|\n/g, '');

		// add direct access to ID'd elements
		this.querySelectorAll('[id]').forEach(elem => {
			const camel = elem.id.replace(/-./g, x => x[1].toUpperCase());
			this[camel] = elem;
		});

		this.querySelectorAll('[l10n]').forEach(elem => {
			const ariaLabel = elem.getAttribute('aria-label');
			const innerHTML = !elem.childElementCount && elem.innerHTML;

			// set keys
			if (ariaLabel) {

			}

		});
	}

	l10n() {

	}
}
