/**
 * Image processor utility for cropping musical notation elements
 */

// Hard-coded image path
const DEFAULT_IMAGE_PATH = '../rec/SheetMusicRec.png';

// Define all available elements and their coordinates
const ELEMENT_DEFINITIONS = {
    // Clefs
    trebleClef: { type: 'rectangle', coords: { x: 87, y: 143, width: 400, height: 600 } },
    bassClef: { type: 'rectangle', coords: { x: 87, y: 897, width: 400, height: 600 } },
    
    // Staff lines (5 lines)
    staff: { type: 'rectangle', coords: { x: 87, y: 1730, width: 4170, height: 480 } },
    
    // Individual notes
    quarterNoteUp: { type: 'rectangle', coords: { x: 1800, y: 143, width: 200, height: 600 } },
    quarterNoteDown: { type: 'rectangle', coords: { x: 1800, y: 897, width: 200, height: 600 } },
    halfNoteUp: { type: 'rectangle', coords: { x: 1800, y: 143, width: 200, height: 600 } },
    halfNoteDown: { type: 'rectangle', coords: { x: 1800, y: 897, width: 200, height: 600 } },
    wholeNote: { type: 'rectangle', coords: { x: 708, y: 878, width: 216, height: 145 } },
    
    // Note flags and beams
    eighthFlagUp: { type: 'rectangle', coords: { x: 2529, y: 143, width: 325, height: 600 } },
    eighthFlagDown: { type: 'rectangle', coords: { x: 2529, y: 897, width: 325, height: 600 } },
    sixteenthFlagUp: { 
        type: 'polygon', 
        coords: [
            { x: 2911, y: 143 },  // Top left
            { x: 3236, y: 143 },  // Top right
            { x: 3236, y: 500 },  // Middle right
            { x: 3100, y: 743 },  // Bottom right (cut off corner)
            { x: 2911, y: 743 }   // Bottom left
        ]
    },
    sixteenthFlagDown: { 
        type: 'polygon', 
        coords: [
            { x: 2911, y: 897 },  // Top left
            { x: 3236, y: 897 },  // Top right
            { x: 3236, y: 1254 },  // Middle right
            { x: 3100, y: 1497 },  // Bottom right (cut off corner)
            { x: 2911, y: 1497 }   // Bottom left
        ]
    },
    
    // Rests
    quarterRest: { type: 'rectangle', coords: { x: 1209, y: 1118, width: 135, height: 370 } },
    halfRest: { type: 'rectangle', coords: { x: 1467, y: 967, width: 220, height: 93 } },
    wholeRest: { type: 'rectangle', coords: { x: 1467, y: 786, width: 220, height: 93 } }
};

// Simplified element mapping for easier access
const ELEMENT_MAP = {
    'staff': 'staff',
    'trebleClef': 'trebleClef',
    'bassClef': 'bassClef',
    'quarter': 'quarterNoteUp',
    'quarterUp': 'quarterNoteUp',
    'quarterDown': 'quarterNoteDown',
    'half': 'halfNoteUp',
    'halfUp': 'halfNoteUp',
    'halfDown': 'halfNoteDown',
    'whole': 'wholeNote',
    'quarterRest': 'quarterRest',
    'halfRest': 'halfRest',
    'wholeRest': 'wholeRest',
    'eighthFlagUp': 'eighthFlagUp',
    'eighthFlagDown': 'eighthFlagDown',
    'sixteenthFlagUp': 'sixteenthFlagUp',
    'sixteenthFlagDown': 'sixteenthFlagDown'
};

// Cache for processed images
const imageCache = new Map();

class MusicImageProcessor {
    constructor(sourceImagePath = DEFAULT_IMAGE_PATH) {
        this.sourceImagePath = sourceImagePath;
        this.canvas = document.createElement('canvas');
        this.ctx = this.canvas.getContext('2d');
        this.initialized = false;
    }

    /**
     * Initialize the processor with the source image
     * @returns {Promise<void>} Resolves when image is loaded
     * @throws {Error} If image fails to load
     */
    async initialize() {
        if (this.initialized) return;

        return new Promise((resolve, reject) => {
            this.sourceImage = new Image();
            this.sourceImage.onload = () => {
                this.canvas.width = this.sourceImage.width;
                this.canvas.height = this.sourceImage.height;
                this.ctx.drawImage(this.sourceImage, 0, 0);
                this.initialized = true;
                resolve();
            };
            this.sourceImage.onerror = (error) => reject(new Error(`Failed to load image: ${error.message}`));
            this.sourceImage.src = this.sourceImagePath;
        });
    }

    /**
     * Crop a section of the image using a polygon shape
     * @param {Array<{x: number, y: number}>} points - Array of points defining the polygon
     * @returns {string} - Data URL of the cropped image
     */
    cropPolygon(points) {
        const bounds = this.calculatePolygonBounds(points);
        const tempCanvas = document.createElement('canvas');
        const tempCtx = tempCanvas.getContext('2d');
        
        tempCanvas.width = bounds.width;
        tempCanvas.height = bounds.height;
        
        tempCtx.drawImage(
            this.sourceImage,
            bounds.x, bounds.y,
            bounds.width, bounds.height,
            0, 0,
            bounds.width, bounds.height
        );
        
        tempCtx.save();
        tempCtx.beginPath();
        tempCtx.moveTo(points[0].x - bounds.x, points[0].y - bounds.y);
        for (let i = 1; i < points.length; i++) {
            tempCtx.lineTo(points[i].x - bounds.x, points[i].y - bounds.y);
        }
        tempCtx.closePath();
        
        const resultCanvas = document.createElement('canvas');
        const resultCtx = resultCanvas.getContext('2d');
        resultCanvas.width = bounds.width;
        resultCanvas.height = bounds.height;
        
        resultCtx.save();
        resultCtx.beginPath();
        resultCtx.moveTo(points[0].x - bounds.x, points[0].y - bounds.y);
        for (let i = 1; i < points.length; i++) {
            resultCtx.lineTo(points[i].x - bounds.x, points[i].y - bounds.y);
        }
        resultCtx.closePath();
        resultCtx.clip();
        resultCtx.drawImage(tempCanvas, 0, 0);
        resultCtx.restore();
        
        return resultCanvas.toDataURL('image/png');
    }

    /**
     * Calculate the bounding rectangle of a polygon
     * @param {Array<{x: number, y: number}>} points - Array of points defining the polygon
     * @returns {{x: number, y: number, width: number, height: number}} - Bounding rectangle
     */
    calculatePolygonBounds(points) {
        const minX = Math.min(...points.map(p => p.x));
        const minY = Math.min(...points.map(p => p.y));
        const maxX = Math.max(...points.map(p => p.x));
        const maxY = Math.max(...points.map(p => p.y));

        return {
            x: minX,
            y: minY,
            width: maxX - minX,
            height: maxY - minY
        };
    }

    /**
     * Crop a section of the image (rectangle or polygon)
     * @param {Object|Array} coordsOrPoints - Either rectangle coords or polygon points
     * @returns {string} - Data URL of the cropped image
     */
    cropSection(coordsOrPoints) {
        if (Array.isArray(coordsOrPoints)) {
            return this.cropPolygon(coordsOrPoints);
        }
        
        const tempCanvas = document.createElement('canvas');
        const tempCtx = tempCanvas.getContext('2d');
        
        tempCanvas.width = coordsOrPoints.width;
        tempCanvas.height = coordsOrPoints.height;
        
        tempCtx.drawImage(
            this.sourceImage,
            coordsOrPoints.x, coordsOrPoints.y,
            coordsOrPoints.width, coordsOrPoints.height,
            0, 0,
            coordsOrPoints.width, coordsOrPoints.height
        );

        return tempCanvas.toDataURL('image/png');
    }
}

/**
 * Create an HTML image element with the specified musical note
 * @param {string} note - The musical note to display (e.g., 'quarter', 'half', 'whole')
 * @param {number} widthPercent - Width as percentage of original size (1-100)
 * @param {number} heightPercent - Height as percentage of original size (1-100)
 * @param {Object} options - Additional options for the image element
 * @returns {Promise<HTMLImageElement>} - The created image element
 * @throws {Error} If note is invalid or image processing fails
 */
async function createNoteImage(note, widthPercent, heightPercent, options = {}) {
    // Validate parameters
    if (!note || typeof note !== 'string') {
        throw new Error('Note parameter is required and must be a string');
    }
    if (widthPercent < 1 || widthPercent > 100) {
        throw new Error('Width percentage must be between 1 and 100');
    }
    if (heightPercent < 1 || heightPercent > 100) {
        throw new Error('Height percentage must be between 1 and 100');
    }

    // Check cache first
    const cacheKey = `${note}-${widthPercent}-${heightPercent}`;
    if (imageCache.has(cacheKey)) {
        const img = new Image();
        img.src = imageCache.get(cacheKey);
        Object.assign(img, options);
        return img;
    }

    const processor = new MusicImageProcessor();
    await processor.initialize();

    const fullElementName = ELEMENT_MAP[note];
    if (!fullElementName || !ELEMENT_DEFINITIONS[fullElementName]) {
        throw new Error(`Invalid note type: ${note}. Available types: ${Object.keys(ELEMENT_MAP).join(', ')}`);
    }

    const element = ELEMENT_DEFINITIONS[fullElementName];
    const imageData = processor.cropSection(element.coords);

    // Create a temporary canvas for resizing
    const tempCanvas = document.createElement('canvas');
    const tempCtx = tempCanvas.getContext('2d');
    const img = new Image();
    
    return new Promise((resolve, reject) => {
        img.onload = () => {
            // Calculate new dimensions based on the original image size
            const newWidth = Math.round((img.width * widthPercent) / 100);
            const newHeight = Math.round((img.height * heightPercent) / 100);
            
            // Set canvas size to the new dimensions
            tempCanvas.width = newWidth;
            tempCanvas.height = newHeight;
            
            // Draw the image at the new size
            tempCtx.drawImage(img, 0, 0, newWidth, newHeight);
            
            // Get the resized image data
            const resizedDataUrl = tempCanvas.toDataURL('image/png');
            
            // Cache the result
            imageCache.set(cacheKey, resizedDataUrl);
            
            // Create a new image with the resized data
            const resizedImg = new Image();
            resizedImg.onload = () => {
                // Apply any additional options
                Object.assign(resizedImg, options);
                resolve(resizedImg);
            };
            resizedImg.onerror = (error) => reject(new Error(`Failed to process resized image: ${error.message}`));
            resizedImg.src = resizedDataUrl;
        };
        
        img.onerror = (error) => reject(new Error(`Failed to process image: ${error.message}`));
        img.src = imageData;
    });
}

// Export the simplified interface
export { createNoteImage }; 