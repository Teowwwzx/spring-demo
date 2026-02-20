// p5.js Global Arena App
let currentUser = null;
let currentRoom = 'arena-1';
let stompClient = null;
let batchPixels = [];
let lastPaintTime = 0;
const BATCH_INTERVAL_MS = 50; // Send batch every 50ms

// UI Elements
const usernameInput = document.getElementById('username-input');
const roomSelect = document.getElementById('room-select');
const loginBtn = document.getElementById('login-btn');
const userPanel = document.getElementById('user-panel');
const statusPanel = document.getElementById('status-panel');
const energyDisplay = document.getElementById('energy-display');
const pixelsDisplay = document.getElementById('pixels-display');
const playerDisplay = document.getElementById('player-display');
const colorPicker = document.getElementById('color-picker');
const toolSelect = document.getElementById('tool-select');
const refreshBtn = document.getElementById('refresh-btn');
const logs = document.getElementById('logs');
const rankingList = document.getElementById('ranking-list');
const roomNameDisplay = document.getElementById('room-name');

// p5.js Functions
function setup() {
    const canvas = createCanvas(1000, 1000);
    canvas.parent('p5-canvas-container');
    background(255);
    pixelDensity(1); // Ensure 1:1 pixel mapping
    noLoop(); // We only draw when updates come or user interacts
}

function draw() {
    // p5.js loop is not strictly needed since we draw on events
}

// WebSocket Connection
function connectWebSocket() {
    const socket = new SockJS('/ws-pixel');
    stompClient = Stomp.over(socket);
    stompClient.debug = null;
    stompClient.connect({}, (frame) => {
        log(`Connected to Real-time Arena`);
        stompClient.subscribe('/topic/pixel-update', (message) => {
            const pixel = JSON.parse(message.body);
            drawRemotePixel(pixel.x, pixel.y, pixel.color);
        });
    }, (error) => {
        log('WebSocket Error: ' + error);
        setTimeout(connectWebSocket, 5000);
    });
}

function drawRemotePixel(x, y, color) {
    stroke(color);
    point(x, y);
}

// User Actions
loginBtn.onclick = async () => {
    const username = usernameInput.value.trim();
    if (!username) return alert('Enter a username');

    try {
        currentRoom = roomSelect.value;
        const response = await fetch(`/api/player/register?username=${username}`, { method: 'POST' });
        if (response.ok) {
            currentUser = username;
            userPanel.classList.add('hidden');
            statusPanel.classList.remove('hidden');
            playerDisplay.innerText = `👤 ${username}`;
            roomNameDisplay.innerText = roomSelect.options[roomSelect.selectedIndex].text;
            log(`Logged in as ${username}`);
            
            connectWebSocket();
            updateStatus();
            refreshCanvas();
            updateRanking();
            updateGlobalStats();
            
            loop(); // Start p5.js loop if needed
        }
    } catch (e) {
        log('Login failed: ' + e.message);
    }
};

async function updateGlobalStats() {
    try {
        const response = await fetch('/api/canvas/stats');
        const data = await response.json();
        document.getElementById('total-painted').innerText = data.totalPixelsPainted.toLocaleString();
        document.getElementById('progress-percent').innerText = data.percentagePainted.toFixed(2);
    } catch (e) { console.error('Global stats failed'); }
}

async function updateStatus() {
    if (!currentUser) return;
    try {
        const response = await fetch(`/api/player/info?username=${currentUser}`);
        const data = await response.json();
        energyDisplay.innerText = `⚡ Energy: ${data.player.energy}/${data.player.maxEnergy}`;
        pixelsDisplay.innerText = `🎨 Painted: ${data.player.pixelsPainted}`;
    } catch (e) { console.error('Status update failed'); }
}

async function updateRanking() {
    try {
        const response = await fetch('/api/player/ranking');
        const players = await response.json();
        rankingList.innerHTML = players.map((p, index) => {
            let rankIcon = `<span>${index + 1}</span>`;
            if (index === 0) rankIcon = '🥇';
            else if (index === 1) rankIcon = '🥈';
            else if (index === 2) rankIcon = '🥉';

            return `
            <li>
                <div class="rank-info">
                    <span class="rank">${rankIcon}</span>
                    <span class="username">${p.username}</span>
                </div>
                <span class="score">🎨 ${p.pixelsPainted}</span>
            </li>
        `}).join('');
    } catch (e) { console.error('Ranking update failed'); }
}

async function refreshCanvas() {
    log('Refreshing canvas...');
    try {
        const response = await fetch('/api/canvas/region?xStart=0&xEnd=1000&yStart=0&yEnd=1000');
        const pixels = await response.json();
        pixels.forEach(p => drawRemotePixel(p.x, p.y, p.color));
        log(`Canvas updated: ${pixels.length} pixels loaded`);
    } catch (e) { log('Refresh failed'); }
}

refreshBtn.onclick = refreshCanvas;

// Interaction
function mouseDragged() {
    if (!currentUser) return;
    if (mouseX < 0 || mouseX >= 1000 || mouseY < 0 || mouseY >= 1000) return;

    const tool = toolSelect.value;
    const color = colorPicker.value;

    if (tool === 'normalBrush') {
        // Smooth line interpolation is handled by drawing many points between pmouse and mouse
        // p5.js provides pmouseX and pmouseY (previous mouse position)
        stroke(color);
        line(pmouseX, pmouseY, mouseX, mouseY);

        // Collect pixels for batching
        // We use a simple linear interpolation to find all pixels between pmouse and mouse
        const pts = getLinePixels(Math.floor(pmouseX), Math.floor(pmouseY), Math.floor(mouseX), Math.floor(mouseY));
        batchPixels.push(...pts);

        // Throttle batch sending
        const now = Date.now();
        if (now - lastPaintTime > BATCH_INTERVAL_MS) {
            sendBatch(color);
            lastPaintTime = now;
        }
    }
    return false; // Prevent default
}

function mousePressed() {
    if (!currentUser) return;
    if (mouseX < 0 || mouseX >= 1000 || mouseY < 0 || mouseY >= 1000) return;

    const tool = toolSelect.value;
    const color = colorPicker.value;

    if (tool !== 'normalBrush') {
        // Special tools (Bomb, Bucket) use single click logic
        sendSinglePaint(Math.floor(mouseX), Math.floor(mouseY), color, tool);
    } else {
        // Initial point for brush
        stroke(color);
        point(mouseX, mouseY);
        batchPixels.push({ x: Math.floor(mouseX), y: Math.floor(mouseY) });
        sendBatch(color);
    }
}

async function sendBatch(color) {
    if (batchPixels.length === 0) return;
    
    const pixelsToSend = [...batchPixels];
    batchPixels = [];

    try {
        await fetch('/api/pixel/paint-batch', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                username: currentUser,
                pixels: pixelsToSend,
                color: color,
                tool: 'normalBrush'
            })
        });
    } catch (e) { console.error('Batch failed', e); }
}

async function sendSinglePaint(x, y, color, tool) {
    const params = new URLSearchParams({
        username: currentUser, x: x, y: y, color: color, tool: tool
    });
    try {
        const response = await fetch(`/api/pixel/paint?${params.toString()}`, { method: 'POST' });
        const result = await response.text();
        if (!result.startsWith('SUCCESS')) log(result);
    } catch (e) { log('Paint failed: ' + e.message); }
}

// Bresenham's Line Algorithm for data collection
function getLinePixels(x0, y0, x1, y1) {
    let pixels = [];
    let dx = Math.abs(x1 - x0);
    let dy = Math.abs(y1 - y0);
    let sx = (x0 < x1) ? 1 : -1;
    let sy = (y0 < y1) ? 1 : -1;
    let err = dx - dy;

    while (true) {
        pixels.push({ x: x0, y: y0 });
        if (x0 === x1 && y0 === y1) break;
        let e2 = 2 * err;
        if (e2 > -dy) { err -= dy; x0 += sx; }
        if (e2 < dx) { err += dx; y0 += sy; }
    }
    return pixels;
}

function log(msg) {
    logs.innerText = `> ${msg}`;
    console.log(msg);
}

// Initial calls
updateRanking();
updateGlobalStats();
setInterval(updateStatus, 3000);
setInterval(updateRanking, 10000);
setInterval(updateGlobalStats, 30000);
