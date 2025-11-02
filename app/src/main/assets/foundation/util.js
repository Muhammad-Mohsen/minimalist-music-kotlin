const Path = (() => {
	const SEPARATOR = /\\|\//g;

	function join(segments) {
		return segments.join('/');
	}

	function eq(one, other) {
		return one.replace(/\/|\\/g, '') == other.replace(/\/|\\/g, '');
	}

	return {
		SEPARATOR,

		join,
		eq,
	}

})();

NodeList.prototype.toArray = function () {
	return [...this];
}
HTMLCollection.prototype.toArray = function () {
	return [...this];
}
HTMLElement.prototype.replayAnimations = function () {
	this.getAnimations().forEach(anim => anim.play());
}
HTMLElement.prototype.cancelAnimations = function () {
	this.getAnimations().forEach(anim => anim.cancel());
}

/**
 * @param {int} steps rotation steps (+ve for right rotations, -ve for left)
 */
Array.prototype.rotate = function (steps) {
	steps = steps % this.length;

	if (!steps) return this; // no rotations, just return the same thing

	if (steps > 0) steps = steps - this.length; // xxxx|xx -- [steps = 2 (right rotation)] == [steps = -4 (left rotation) ==> steps - length]

	steps *= -1;
	const left = this.slice(0, steps);
	const right = this.slice(steps, this.length);

	return [...right, ...left];
}

String.prototype.fuzzyCompare = function (possible) {
	const match = this.match(new RegExp(possible, 'i'));
	return match ? Array.from({ length: possible.length }, (_, i) => match.index + i) : null;
}
String.prototype.replaceAt = function (index, replacement) {
	return this.substring(0, index) + replacement + this.substring(index + 1);
}

function readableTime(millis, format) {
	const seconds = millis / 1000;

	const parts = {
		ss: parseInt(seconds % 60).toString().padStart(2, '0'),
		mm: parseInt((seconds / 60) % 60).toString().padStart(2, '0'),
		hh: parseInt(seconds / 60 / 60).toString().padStart(2, '0'),
	}

	return format
		? format.replace(/hh|mm|ss/g, (match) => parts[match])
		: parts.hh == '00' ? `${parts.mm}:${parts.ss}` : `${parts.hh}:${parts.mm}:${parts.ss}`;
}

// "when"
class WhenExpression {

	#done;
	#result;

	constructor(param) {
		this.param = param;
	}

	is = (val, callback) => {
		if (this.#result != undefined || this.#done) return this;

		if (this.param == val // simple value
			|| (Array.isArray(val) && val.includes(this.param)) // array
			|| val === true // expression
		) {
			this.#result = callback();
			this.#done = true;
		}
		return this;
	}

	val = () => this.#result;
}

function when(param) {
	return new WhenExpression(param);
}
