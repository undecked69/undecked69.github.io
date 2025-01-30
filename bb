<!DOCTYPE html>
<html lang="hu">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>AGRO Quality Inspector - Munka Napló</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
    
    <!-- Firebase SDK -->
    <script src="https://www.gstatic.com/firebasejs/8.10.0/firebase-app.js"></script>
    <script src="https://www.gstatic.com/firebasejs/8.10.0/firebase-firestore.js"></script>

    <style>
        :root {
            --primary-color: #2c5f2d;
            --secondary-color: #004e89;
        }

        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            max-width: 800px;
            margin: 0 auto;
            padding: 20px;
            background: #f8f9fa;
        }

        .work-form {
            background: white;
            padding: 25px;
            border-radius: 10px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            margin-bottom: 30px;
        }

        .form-group {
            margin: 15px 0;
        }

        input, select {
            width: 100%;
            padding: 12px;
            border: 1px solid #ddd;
            border-radius: 5px;
            margin-top: 5px;
            appearance: none;
            -webkit-appearance: none;
            background: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="%232c5f2d"><path d="M7 10l5 5 5-5z"/></svg>') no-repeat right 10px center/15px;
        }

        button {
            background: var(--primary-color);
            color: white;
            border: none;
            padding: 15px 30px;
            border-radius: 5px;
            cursor: pointer;
            width: 100%;
        }

        .work-list {
            background: white;
            padding: 25px;
            border-radius: 10px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }

        .work-item {
            border-bottom: 1px solid #eee;
            padding: 15px 0;
        }

        .calculator {
            position: fixed;
            bottom: 20px;
            right: 20px;
            background: white;
            padding: 20px;
            border-radius: 10px;
            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
            width: 300px;
        }

        .calculator h3 {
            margin-top: 0;
            color: var(--primary-color);
            border-bottom: 2px solid #eee;
            padding-bottom: 10px;
        }

        @media (max-width: 768px) {
            .calculator {
                position: static;
                width: auto;
                margin-top: 20px;
            }
        }
    </style>
</head>
<body>
    <h1>AGRO Munka Napló <i class="fas fa-clipboard-list"></i></h1>

    <div class="work-form">
        <h2>Új munka rögzítése</h2>
        <form id="workForm">
            <div class="form-group">
                <label>Megbízó:</label>
                <select id="company" required>
                    <option value="" disabled selected>Válassz megbízót...</option>
                    <option value="SGS">SGS</option>
                    <option value="Bureau Veritas">Bureau Veritas</option>
                    <option value="AmSpec">AmSpec</option>
                    <option value="Control Union">Control Union</option>
                </select>
            </div>

            <div class="form-group">
                <label>Helyszín:</label>
                <input type="text" id="location" required>
            </div>

            <div class="form-group">
                <label>Órák száma:</label>
                <input type="number" id="hours" step="0.5" required>
            </div>

            <div class="form-group">
                <label>Megtett kilóméter:</label>
                <input type="number" id="kilometers" required>
            </div>

            <button type="submit"><i class="fas fa-save"></i> Mentés</button>
        </form>
    </div>

    <div class="work-list">
        <h2>Korábbi bejegyzések</h2>
        <div id="workEntries"></div>
    </div>

    <div class="calculator">
        <h3><i class="fas fa-calculator"></i> Bérszámoló</h3>
        <p>Összes óra: <span id="totalHours">0</span></p>
        <p>Összes km: <span id="totalKm">0</span></p>
        <p><strong>Teljes díjazás:</strong> <span id="totalPayment">0 Ft</span></p>
    </div>

    <script>
        // Firebase konfiguráció (CSERÉLD KI A SAJÁT ADATAIDRA!)
        const firebaseConfig = {
            apiKey: "AIzaSyYOUR_API_KEY",
            authDomain: "your-project-id.firebaseapp.com",
            projectId: "your-project-id",
            storageBucket: "your-project-id.appspot.com",
            messagingSenderId: "1234567890",
            appId: "1:1234567890:web:abcdef123456"
        };

        // Firebase inicializálása
        firebase.initializeApp(firebaseConfig);
        const db = firebase.firestore();

        // Tarifák
        const rates = {
            'SGS': { hour: 1300, km: 70 },
            'Bureau Veritas': { hour: 1500, km: 90 },
            'AmSpec': { fixed: 30000 },
            'Control Union': { fixed: 20000 }
        };

        // Űrlap mentése
        document.getElementById('workForm').addEventListener('submit', async (e) => {
            e.preventDefault();

            const workEntry = {
                company: document.getElementById('company').value,
                location: document.getElementById('location').value,
                hours: parseFloat(document.getElementById('hours').value),
                kilometers: parseInt(document.getElementById('kilometers').value),
                timestamp: firebase.firestore.FieldValue.serverTimestamp()
            };

            try {
                await db.collection('workEntries').add(workEntry);
                alert('Sikeres mentés!');
                document.getElementById('workForm').reset();
                loadEntries();
            } catch (error) {
                console.error("Hiba:", error);
                alert('Hiba történt a mentés során!');
            }
        });

        // Bejegyzések betöltése
        async function loadEntries() {
            try {
                const querySnapshot = await db.collection('workEntries')
                    .orderBy('timestamp', 'desc')
                    .get();
                
                let html = '';
                let totalPayment = 0;
                let totalHours = 0;
                let totalKm = 0;

                querySnapshot.forEach(doc => {
                    const data = doc.data();
                    const payment = calculatePayment(data);
                    
                    totalPayment += payment;
                    totalHours += data.hours;
                    totalKm += data.kilometers;

                    html += `
                        <div class="work-item">
                            <p><strong>${data.company}</strong> • ${data.location}</p>
                            <p>Óra: ${data.hours} óra • KM: ${data.kilometers} km</p>
                            <p>Díj: ${payment.toLocaleString()} Ft</p>
                            <small>${data.timestamp?.toDate().toLocaleDateString('hu-HU')}</small>
                        </div>
                    `;
                });

                document.getElementById('workEntries').innerHTML = html;
                updateCalculator(totalHours, totalKm, totalPayment);
            } catch (error) {
                console.error("Hiba az adatok betöltésekor:", error);
            }
        }

        // Díj számolása
        function calculatePayment(entry) {
            const rate = rates[entry.company];
            if (!rate) return 0;

            if (rate.fixed) {
                return rate.fixed;
            } else {
                return (entry.hours * rate.hour) + (entry.kilometers * rate.km);
            }
        }

        // Számoló frissítése
        function updateCalculator(hours, km, payment) {
            document.getElementById('totalHours').textContent = hours.toFixed(1);
            document.getElementById('totalKm').textContent = km;
            document.getElementById('totalPayment').textContent = 
                `${payment.toLocaleString('hu-HU')} Ft`;
        }

        // Induláskor betöltjük az adatokat
        loadEntries();
    </script>
</body>
</html>
