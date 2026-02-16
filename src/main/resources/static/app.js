const canvas = document.getElementById('pixel-canvas');
const ctx = canvas.getContext('2d');
const usernameInput = document.getElementById('username-input');
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

let currentUser = null;
let stompClient = null;

// Initialize Canvas
ctx.fillStyle = '#FFFFFF';
ctx.fillRect(0, 0, 1000, 1000);

// WebSocket Connection
function connectWebSocket() {
    const socket = new SockJS('/ws-pixel');
    stompClient = Stomp.over(socket);
    stompClient.debug = null; // Disable debug logs to keep console clean
    
    stompClient.connect({}, (frame) => {
        log('Connected to Real-time Arena');
        stompClient.subscribe('/topic/pixel-update', (message) => {
            const pixel = JSON.parse(message.body);
            ctx.fillStyle = pixel.color;
            ctx.fillRect(pixel.x, pixel.y, 1, 1);
        });
    }, (error) => {
        log('WebSocket Error: ' + error);
        setTimeout(connectWebSocket, 5000); // Try to reconnect
    });
}

// Login Logic
loginBtn.onclick = async () => {
    const username = usernameInput.value.trim();
    if (!username) return alert('Enter a username');

    try {
        const response = await fetch(`/api/player/register?username=${username}`, { method: 'POST' });
        if (response.ok) {
            currentUser = username;
            userPanel.classList.add('hidden');
            statusPanel.classList.remove('hidden');
            playerDisplay.innerText = `👤 ${username}`;
            log(`Logged in as ${username}`);
            
            connectWebSocket();
            updateStatus();
            refreshCanvas();
            setInterval(updateStatus, 3000); // Update status every 3s
        }
    } catch (e) {
        log('Login failed: ' + e.message);
    }
};

// Update Player Status
async function updateStatus() {
    if (!currentUser) return;
    try {
        const response = await fetch(`/api/player/info?username=${currentUser}`);
        const data = await response.json();
        energyDisplay.innerText = `⚡ Energy: ${data.player.energy}/${data.player.maxEnergy}`;
        pixelsDisplay.innerText = `🎨 Painted: ${data.player.pixelsPainted}`;
    } catch (e) { console.error('Status update failed'); }
}

// Refresh Canvas (Simple version: fetch all painted pixels)
async function refreshCanvas() {
    log('Refreshing canvas...');
    try {
        // For demo, we fetch a region around the center or visible area
        // In a real app, we might fetch only what's changed
        const response = await fetch('/api/canvas/region?xStart=0&xEnd=1000&yStart=0&yEnd=1000');
        const pixels = await response.json();
        
        pixels.forEach(p => {
            ctx.fillStyle = p.color;
            ctx.fillRect(p.x, p.y, 1, 1);
        });
        log(`Canvas updated: ${pixels.length} pixels loaded`);
    } catch (e) { log('Refresh failed'); }
}

refreshBtn.onclick = refreshCanvas;

// Painting Logic
canvas.onclick = async (e) => {
    if (!currentUser) return alert('Please join the arena first!');

    const rect = canvas.getBoundingClientRect();
    const x = Math.floor(e.clientX - rect.left);
    const y = Math.floor(e.clientY - rect.top);
    const color = colorPicker.value;
    const tool = toolSelect.value;

    log(`Painting at (${x}, ${y}) with ${tool}...`);

    try {
        const params = new URLSearchParams({
            username: currentUser,
            x: x,
            y: y,
            color: color,
            tool: tool
        });

        const response = await fetch(`/api/pixel/paint?${params.toString()}`, { method: 'POST' });
        const result = await response.text();
        
        log(result);
        
        if (result.startsWith('SUCCESS')) {
            updateStatus();
        }
    } catch (e) {
        log('Paint failed: ' + e.message);
    }
};

function log(msg) {
    logs.innerText = `> ${msg}`;
    console.log(msg);
}
