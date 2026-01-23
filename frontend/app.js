// Use window.API_URL if set, otherwise default to /api/wordsearch
const apiUrl = window.API_URL || "/api/wordsearch";

document.getElementById('wordsearch-form').addEventListener('submit', async function(e) {
    e.preventDefault();
    const words = document.getElementById('words').value;
    const pdf = document.getElementById('pdf').checked;
    const resultDiv = document.getElementById('result');
    resultDiv.innerHTML = 'Loading...';

    try {
        const footerUrl = window.PDF_FOOTER_URL || "";
        const response = await fetch(apiUrl, {
            headers: { 'Content-Type': 'application/json' },
            method: 'POST',
            body: JSON.stringify({ words: words.split(',').map(w => w.trim()), pdf, footerUrl })
        });
        const contentType = response.headers.get('Content-Type') || '';
        if (pdf) {
            if (contentType.includes('application/pdf')) {
                const blob = await response.blob();
                const url = URL.createObjectURL(blob);
                resultDiv.innerHTML = `<a href="${url}" target="_blank">View PDF</a>`;
                return;
            } else if (contentType.includes('application/json')) {
                const data = await response.json();
                if (data.error) {
                    resultDiv.innerHTML = 'Error: ' + data.error;
                } else {
                    resultDiv.innerHTML = 'Error: Unexpected JSON response.';
                }
                return;
            } else {
                resultDiv.innerHTML = 'Error: Unexpected response type: ' + contentType;
                return;
            }
        } else {
            if (contentType.includes('application/json')) {
                const data = await response.json();
                if (data.error) {
                    resultDiv.innerHTML = 'Error: ' + data.error;
                    return;
                }
                let grid = data.grid;
                // Handle grid as string or array
                if (typeof grid === 'string') {
                    // Split by newlines, then by characters or spaces
                    grid = grid.split('\n').map(row => row.split(/\s+/));
                }
                let html = '<h2>Word Search Grid</h2><table class="grid">';
                grid.forEach(row => {
                    // If row is a string, split into characters
                    if (typeof row === 'string') {
                        row = row.split('');
                    }
                    html += '<tr>' + row.map(cell => `<td>${cell}</td>`).join('') + '</tr>';
                });
                html += '</table>';
                html += '<h3>Words:</h3><ul>' + data.words.map(w => `<li>${w}</li>`).join('') + '</ul>';
                resultDiv.innerHTML = html;
            } else {
                resultDiv.innerHTML = 'Error: Unexpected response type: ' + contentType;
            }
        }
    } catch (err) {
        resultDiv.innerHTML = 'Error: ' + err;
    }
});
