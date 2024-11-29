// Set default min and max values (MIDI numbers, but within piano's 88-key range)
const defaultMinNote = 48; // C3 (one octave below middle C)
const defaultMaxNote = defaultMinNote + 12; // C4 (middle C)
const pianoMaxKeys = 88; // Total number of keys on a standard piano

function getCustomRange() {
    // Get the min and max values from the input elements
    const min = parseInt(document.getElementById('minNote').value) || defaultMinNote;
    const max = parseInt(document.getElementById('maxNote').value) || defaultMaxNote;

    // Ensure values are between 1 and 88 (total number of piano keys)
    if (min < 1 || min > pianoMaxKeys || max < 1 || max > pianoMaxKeys) {
        alert('Please enter values between 1 and 88.');
        return { min: defaultMinNote, max: defaultMaxNote };
    }

    // Ensure min is less than or equal to max
    if (min > max) {
        alert('Min note cannot be greater than max note.');
        return { min: defaultMinNote, max: defaultMaxNote };
    }

    return { min, max };
}

function playRandomNote() {
    const { min, max } = getCustomRange();

    // Generate a random note within the range
    const randomNote = Math.floor(Math.random() * (max - min + 1)) + min;

    console.log(`Playing a random note between ${min} and ${max}: ${randomNote}`);
    // Your note-playing logic here
}