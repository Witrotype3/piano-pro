function windowLoading(){
    try{
        const loading = document.getElementById("loaded");
        if (loading) {
            loading.setAttribute("id", "loading");
        }
    } catch(error) {
        console.error("Error during window loading:", error);
    }
}

function windowLoaded(){
    try{
        const loaded = document.getElementById("loading");
        if (loaded) {
            loaded.setAttribute("id", "loaded");
        }
    } catch(error) {
        console.error("Error during window loaded:", error);
    }
}

function wait(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

// Generic function to fetch and inject HTML content
async function fetchAndInjectHTML(url, targetId) {
    try {
        const response = await fetch(url);
        if (!response.ok) {
            throw new Error(`Network response was not ok: ${response.statusText}`);
        }
        const data = await response.text();
        const targetElement = document.getElementById(targetId);
        if (targetElement) {
            targetElement.innerHTML = data;
        }
    } catch (error) {
        console.error(`Error fetching ${url}:`, error);
    }
}

function createHeader() {
    fetchAndInjectHTML('/piano-pro/views/header.html', 'header');
}

function createHome() {
    fetchAndInjectHTML('/piano-pro/views/home.html', 'body');
}

function createPianoNotes() {
    fetchAndInjectHTML('/piano-pro/views/piano_notes.html', 'body');
}

function createPitchPerfect() {
    fetchAndInjectHTML('/piano-pro/views/pitch_perfect.html', 'body');
}

function createTreble() {
    fetchAndInjectHTML('/piano-pro/views/treble.html', 'body');
}

function createPiano() {
    fetchAndInjectHTML('/piano-pro/views/piano.html', 'piano');
}

async function loadPageContent() {
    windowLoading();
    createHeader();
    createHome();
    createPiano();
    
    // Wait for content to load dynamically
    const checkContentLoaded = () => {
        const header = document.getElementById('header');
        const home = document.getElementById('body');
        const piano = document.getElementById('piano');
        return header && home && piano;
    };
    
    // Poll until content is loaded
    while (!checkContentLoaded()) {
        await wait(50);
    }
    
    windowLoaded();
}

window.addEventListener('load', () => {
    loadPageContent();
});