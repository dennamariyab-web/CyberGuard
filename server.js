const express = require('express');
const path = require('path');
const fs = require('fs');

const app = express();
const PORT = 3000;
const dataFile = path.join(__dirname, 'data.json');

app.use(express.json());
app.use(express.static(path.join(__dirname, 'public')));

// Helper to read data
function readData() {
    try {
        const data = fs.readFileSync(dataFile, 'utf8');
        return JSON.parse(data);
    } catch (err) {
        return { user: {}, logs: [] };
    }
}

// Helper to write data
function writeData(data) {
    fs.writeFileSync(dataFile, JSON.stringify(data, null, 2), 'utf8');
}

// Get user profile
app.get('/api/user', (req, res) => {
    const data = readData();
    res.json(data.user || {});
});

// Update user profile
app.post('/api/user', (req, res) => {
    const data = readData();
    data.user = { ...data.user, ...req.body };
    writeData(data);
    res.json(data.user);
});

// Get food logs for today
app.get('/api/logs', (req, res) => {
    const data = readData();
    // In a real app we'd filter by date, but we keep it simple for now
    res.json(data.logs || []);
});

// Add food log
app.post('/api/logs', (req, res) => {
    const data = readData();
    const newLog = {
        id: Date.now(),
        date: new Date().toISOString(),
        ...req.body
    };
    data.logs = data.logs || [];
    data.logs.push(newLog);
    writeData(data);
    res.json(newLog);
});

// Delete food log
app.delete('/api/logs/:id', (req, res) => {
    const data = readData();
    const id = parseInt(req.params.id, 10);
    if (data.logs) {
        data.logs = data.logs.filter(log => log.id !== id);
        writeData(data);
    }
    res.json({ success: true });
});

app.listen(PORT, () => {
    console.log(`Server running at http://localhost:${PORT}`);
});
