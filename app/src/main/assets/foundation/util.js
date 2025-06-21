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
	const lower = this.toLowerCase();
	possible = possible.toLowerCase();

	let currentIndex = 0;
	let matchedIndices = [];
	for (let c of possible) {
		currentIndex = lower.indexOf(c, currentIndex) + 1;
		if (currentIndex == 0) return false;
		else matchedIndices.push(currentIndex - 1);
	}

	return matchedIndices;
}
String.prototype.replaceAt = function (index, replacement) {
	return this.substring(0, index) + replacement + this.substring(index + 1);
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
