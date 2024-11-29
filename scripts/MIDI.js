window.AudioContext = window.AudioContext || window.webkitAudioContext;
let ctx;
const startButton = document.querySelector('body');
const audioBuffers = new Map(); // Store AudioBuffers for each note
const oscillators = new Map();  // Track active oscillators (notes currently being played)
const oscillatorsPressed = new Map(); // Track notes physically pressed
const sustainedNotes = new Set(); // Track sustained notes (notes held with sustain pedal)
let sustainOn = false; // Track sustain pedal state
let midiConected = false; //Track midi conection
const pathToPianoMP3 = "../rec/piano-mp3";

//load all audio files
function loadAudio(){
    if(!ctx) return;
    for(let note = 22; note < 89; note++){
        fetch(`${pathToPianoMP3}/${note}.mp3`)
        .then(response => response.arrayBuffer())
        .then(data => {
            ctx.decodeAudioData(data, (buffer) => {
                audioBuffers.set(note, buffer);
            });
        })
        .catch(err => console.error('Error loading audio buffer:', err));
    }
}

// Event listener for the start button
startButton.addEventListener('click', () => {
    // Initialize the AudioContext when the button is clicked
    ctx = new AudioContext();
    startButton.disabled = true; // Disable the button after starting the context
    ctx.resume(); // Ensure AudioContext is resumed (browsers often block autoplay)

    if (navigator.requestMIDIAccess) {
        navigator.requestMIDIAccess().then(success, failure);
    }
});

// Success callback for MIDI access
function success(midiAccess) {
    if(midiConected) return;
    console.log("MIDI connected");
    midiConected = true;
    midiAccess.addEventListener('statechange', updateDevices);

    const inputs = midiAccess.inputs;
    inputs.forEach(input => {
        input.addEventListener('midimessage', handleInput);
    });
    loadAudio();
}

// Handle incoming MIDI messages
function handleInput(input) {
    const [command, note, velocity] = input.data;

    switch (command) {
        case 144: // noteOn
            velocity > 0 ? noteOn(note, velocity) : noteOff(note);
            break;
        case 128: // noteOff
            noteOff(note);
            break;
        case 176: // sustain pedal
            handleSustainPedal(velocity);
            break;
    }
}

// Start note by playing the corresponding audio buffer
function noteOn(note, velocity) {
    if (!ctx) return; // Ensure AudioContext is initialized

    // If the note is already playing, stop it first (this handles retriggering the same note)
    if (oscillators.has(note)) {
        noteOff(note); // Stop the previous instance of the note immediately
    }

    // If the audio buffer for this note isn't already loaded, load it
    if (!audioBuffers.has(note)) {
        fetch(`${pathToPianoMP3}/${note}.mp3`)
            .then(response => response.arrayBuffer())
            .then(data => {
                ctx.decodeAudioData(data, (buffer) => {
                    audioBuffers.set(note, buffer);
                    playNoteBuffer(note, velocity); // Play the note after buffer is loaded
                });
            })
            .catch(err => console.error('Error loading audio buffer:', err));
    } else {
        playNoteBuffer(note, velocity); // Play the note if already loaded
    }
}

// Function to play the note using AudioBufferSourceNode
function playNoteBuffer(note, velocity) {
    if (!ctx) return;

    const audioBuffer = audioBuffers.get(note);

    // Create a new GainNode to control the volume
    const gainNode = ctx.createGain();
    gainNode.gain.setValueAtTime(Math.min(velocity / 60, 1), ctx.currentTime); // Set initial volume based on velocity
    gainNode.connect(ctx.destination); // Connect the gain node to the AudioContext output

    // Create a new AudioBufferSourceNode
    const source = ctx.createBufferSource();
    source.buffer = audioBuffer;
    source.connect(gainNode); // Connect source to gain node

    // Play the audio
    source.start(0); // Start immediately
    source.onended = () => {
        // When the note finishes playing, remove it from the active oscillators
        oscillators.delete(note);
        if (sustainOn) {
            sustainedNotes.delete(note); // Remove the note from sustained notes when it ends
        }
    };

    // Track the currently playing note and the corresponding gain node
    oscillators.set(note, { source, gainNode });
    oscillatorsPressed.set(note, source); // Track physically pressed notes

    // If sustain pedal is on, mark this note as sustained
    if (sustainOn) {
        sustainedNotes.add(note);
    }
}

// Stop note with volume fade-out or immediately if sustain pedal is off
function noteOff(note) {
    if (oscillators.has(note)) {
        const { source, gainNode } = oscillators.get(note);
        oscillatorsPressed.delete(note);

        // If sustain pedal is off, stop the note immediately with a fade-out
        if (!sustainOn) {
            const fadeOutTime = 0.05; // Adjust fade-out duration (seconds)
            const interval = 20; // Adjust interval for smoother fade
            let currentVolume = gainNode.gain.value;
            const fadeOutInterval = setInterval(() => {
                currentVolume -= currentVolume / (fadeOutTime * (1000 / interval));
                gainNode.gain.setValueAtTime(Math.max(0, currentVolume), ctx.currentTime);
                if (currentVolume <= 0.001) {
                    clearInterval(fadeOutInterval);
                    source.stop(); // Stop the audio buffer playback
                    oscillators.delete(note);
                }
            }, interval);
        } else {
            // If sustain pedal is on, add to sustained notes but don't stop yet
            sustainedNotes.add(note);
        }
    } else if (sustainedNotes.has(note)) {
        // If the note is in sustained notes, ensure we stop it correctly when the pedal is off
        sustainedNotes.delete(note);
    }
}

// Handle sustain pedal press and release
function handleSustainPedal(velocity) {
    sustainOn = velocity > 0; // If velocity is greater than 0, pedal is pressed

    // If pedal is released, stop all non-pressed notes that are sustained
    if (!sustainOn) {
        sustainedNotes.forEach(note => {
            if (!oscillatorsPressed.has(note)) {
                noteOff(note); // Stop non-pressed sustained notes
            }
        });
    }
}

// Update MIDI devices when their state changes
function updateDevices(event) {
    // Optional: You can log or process device updates here
}

// Failure callback for MIDI access
function failure() {
    console.log('Could not connect to MIDI');
}

function check(id, key) {
    key.classList.toggle('blue');
    noteOn(id, 127);
    execute(key);
}

async function execute(theKey) {
    await wait(90); // Wait
    theKey.classList.remove('blue');
}