// MIDI2.js - MIDI connection handler with user interaction requirement

let midiInitialized = false;

// Array to track state of all 88 piano keys (false = not pressed, true = pressed)
const pianoKeys = new Array(88).fill(false);

// Function to initialize MIDI connection
async function initializeMIDI() {
    if (midiInitialized) return;
    
    try {
        // Request MIDI access
        const midiAccess = await navigator.requestMIDIAccess();
        console.log('MIDI access granted');
        
        // Handle MIDI inputs
        midiAccess.inputs.forEach(input => {
            console.log('MIDI input:', input.name);
            input.onmidimessage = handleMIDIMessage;
        });
        
        // Handle MIDI outputs
        midiAccess.outputs.forEach(output => {
            console.log('MIDI output:', output.name);
        });
        
        midiInitialized = true;
    } catch (error) {
        console.error('Error accessing MIDI:', error);
    }
}

// Function to handle MIDI messages
function handleMIDIMessage(event) {
    // MIDI message format: [status, note, velocity]
    const [status, note, velocity] = event.data;
    
    // MIDI note numbers for piano keys start at 21 (A0) and end at 108 (C8)
    // We need to map these to our 88-key array (0-87)
    const pianoKeyIndex = note - 21;
    
    // Check if the note is within our piano range
    if (pianoKeyIndex >= 0 && pianoKeyIndex < 88) {
        // Note On message (144 = 0x90)
        if (status === 144 && velocity > 0) {
            pianoKeys[pianoKeyIndex] = true;
        }
        // Note Off message (128 = 0x80) or Note On with velocity 0
        else if (status === 128 || (status === 144 && velocity === 0)) {
            pianoKeys[pianoKeyIndex] = false;
        }
    }
}

// Add event listeners for user interaction
document.addEventListener('click', () => {
    if (!midiInitialized) {
        initializeMIDI();
    }
}, { once: true });

document.addEventListener('keydown', () => {
    if (!midiInitialized) {
        initializeMIDI();
    }
}, { once: true });

document.addEventListener('touchstart', () => {
    if (!midiInitialized) {
        initializeMIDI();
    }
}, { once: true });

// Export functions and piano keys state
window.MIDI2 = {
    initializeMIDI,
    handleMIDIMessage,
    pianoKeys // Export the piano keys array so other scripts can access it
};
