document.addEventListener('DOMContentLoaded', () => {
    // State
    let user = {};
    let logs = [];
    let targets = {
        calories: 2000,
        carbs: 250,
        protein: 100,
        fat: 65
    };

    // DOM Elements
    const els = {
        heightVal: document.getElementById('heightVal'),
        weightVal: document.getElementById('weightVal'),
        ageVal: document.getElementById('ageVal'),
        sexVal: document.getElementById('sexVal'),
        bmiVal: document.getElementById('bmiVal'),
        bmiStatus: document.getElementById('bmiStatus'),
        bmiThumb: document.getElementById('bmiThumb'),
        guidanceList: document.getElementById('guidanceList'),
        calorieDonut: document.getElementById('calorieDonut'),
        caloriesEaten: document.getElementById('caloriesEaten'),
        caloriesTotal: document.getElementById('caloriesTotal'),
        caloriesLeft: document.getElementById('caloriesLeft'),
        dailyTargetKcal: document.getElementById('dailyTargetKcal'),
        carbsLabel: document.getElementById('carbsLabel'),
        carbsProgress: document.getElementById('carbsProgress'),
        proteinLabel: document.getElementById('proteinLabel'),
        proteinLabel: document.getElementById('proteinLabel'),
        proteinProgress: document.getElementById('proteinProgress'),
        fatLabel: document.getElementById('fatLabel'),
        fatProgress: document.getElementById('fatProgress'),
        logList: document.getElementById('logList'),
        emptyJournal: document.getElementById('emptyJournal'),
        waterVal: document.getElementById('waterVal'),
        
        // Modals
        bodyModal: document.getElementById('bodyModal'),
        mealModal: document.getElementById('mealModal'),
        editBodyBtn: document.getElementById('editBodyBtn'),
        logMealBtn: document.getElementById('logMealBtn'),
        closeBodyModal: document.getElementById('closeBodyModal'),
        closeMealModal: document.getElementById('closeMealModal'),
        
        // Forms
        bodyForm: document.getElementById('bodyForm'),
        mealForm: document.getElementById('mealForm'),
        foodName: document.getElementById('foodName'),
        foodAmount: document.getElementById('foodAmount'),
        foodCalories: document.getElementById('foodCalories'),
        foodCarbs: document.getElementById('foodCarbs'),
        foodProtein: document.getElementById('foodProtein'),
        foodFat: document.getElementById('foodFat'),
        foodOptions: document.getElementById('foodOptions')
    };

    const foodDB = {
        "Apple": { cal: 52, carb: 14, pro: 0.3, fat: 0.2 },
        "Banana": { cal: 89, carb: 23, pro: 1.1, fat: 0.3 },
        "Chicken Breast": { cal: 165, carb: 0, pro: 31, fat: 3.6 },
        "White Rice (cooked)": { cal: 130, carb: 28, pro: 2.7, fat: 0.3 },
        "Oats (dry)": { cal: 389, carb: 66, pro: 16.9, fat: 6.9 },
        "Egg": { cal: 143, carb: 0.7, pro: 12.6, fat: 9.5 },
        "Salmon": { cal: 208, carb: 0, pro: 20, fat: 13 },
        "Avocado": { cal: 160, carb: 8.5, pro: 2, fat: 14.7 },
        "Greek Yogurt": { cal: 59, carb: 3.6, pro: 10, fat: 0.4 },
        "Almonds": { cal: 579, carb: 21.6, pro: 21.1, fat: 49.9 },
        "Sweet Potato": { cal: 86, carb: 20.1, pro: 1.6, fat: 0.1 },
        "Olive Oil": { cal: 884, carb: 0, pro: 0, fat: 100 },
        "Tofu": { cal: 76, carb: 1.9, pro: 8, fat: 4.8 },
        "Spinach": { cal: 23, carb: 3.6, pro: 2.9, fat: 0.4 },
        "Kale": { cal: 49, carb: 8.8, pro: 4.3, fat: 0.9 },
        "Lettuce": { cal: 15, carb: 2.9, pro: 1.4, fat: 0.2 },
        "Carrot": { cal: 41, carb: 9.6, pro: 0.9, fat: 0.2 },
        "Orange": { cal: 47, carb: 11.8, pro: 0.9, fat: 0.1 },
        "Grape": { cal: 69, carb: 18.1, pro: 0.7, fat: 0.2 },
        "Broccoli": { cal: 34, carb: 6.6, pro: 2.8, fat: 0.4 },
        "Strawberry": { cal: 32, carb: 7.7, pro: 0.7, fat: 0.3 },
        "Watermelon": { cal: 30, carb: 7.6, pro: 0.6, fat: 0.2 },
        "Cabbage": { cal: 25, carb: 5.8, pro: 1.3, fat: 0.1 },
        "Beef": { cal: 250, carb: 0, pro: 26, fat: 15 },
        "Pork": { cal: 242, carb: 0, pro: 27, fat: 14 },
        "Beetroot": { cal: 43, carb: 10, pro: 1.6, fat: 0.2 },
        "Legume": { cal: 116, carb: 21, pro: 8, fat: 0.4 },
        "Fish": { cal: 206, carb: 0, pro: 22, fat: 12 }
    };

    // Populate datalist
    els.foodOptions.innerHTML = Object.keys(foodDB).map(food => `<option value="${food}">`).join('');

    async function fetchFoodData(query) {
        try {
            const res = await fetch(`https://world.openfoodfacts.org/cgi/search.pl?search_terms=${encodeURIComponent(query)}&search_simple=1&action=process&json=1&page_size=1`);
            const data = await res.json();
            if (data.products && data.products.length > 0) {
                const nut = data.products[0].nutriments;
                return {
                    cal: nut['energy-kcal_100g'] || nut['energy_100g'] / 4.184 || 0,
                    carb: nut['carbohydrates_100g'] || 0,
                    pro: nut['proteins_100g'] || 0,
                    fat: nut['fat_100g'] || 0
                };
            }
        } catch (e) {
            console.error("API fetch failed", e);
        }
        return null;
    }

    async function autoFillFood() {
        const name = els.foodName.value.trim();
        if (!name) return;
        const amount = Number(els.foodAmount.value) || 100;
        
        let match = foodDB[name.charAt(0).toUpperCase() + name.slice(1).toLowerCase()] || foodDB[name];
        if (!match) {
            els.foodName.style.opacity = '0.5';
            match = await fetchFoodData(name);
            els.foodName.style.opacity = '1';
        }

        if (match) {
            const multiplier = amount / 100;
            els.foodCalories.value = Math.round(match.cal * multiplier);
            els.foodCarbs.value = (match.carb * multiplier).toFixed(1);
            els.foodProtein.value = (match.pro * multiplier).toFixed(1);
            els.foodFat.value = (match.fat * multiplier).toFixed(1);
        }
    }

    els.foodName.addEventListener('blur', autoFillFood);
    els.foodAmount.addEventListener('blur', autoFillFood);

    // Greeting Time
    const hour = new Date().getHours();
    const greeting = document.getElementById('greeting');
    if(hour < 12) greeting.textContent = "Good morning, Friend.";
    else if(hour < 18) greeting.textContent = "Good afternoon, Friend.";
    else greeting.textContent = "Good evening, Friend.";

    // Swipe Indicator Logic
    const swipeContainer = document.getElementById('swipeContainer');
    const dots = document.querySelectorAll('.dot');
    if (swipeContainer && dots.length > 0) {
        swipeContainer.addEventListener('scroll', () => {
            const index = Math.round(swipeContainer.scrollLeft / swipeContainer.offsetWidth);
            dots.forEach((dot, i) => {
                dot.classList.toggle('active', i === index);
            });
        });
        dots.forEach((dot, i) => {
            dot.addEventListener('click', () => {
                swipeContainer.scrollTo({
                    left: i * swipeContainer.offsetWidth,
                    behavior: 'smooth'
                });
            });
        });
    }

    // Modal Logic
    const openModal = (modal) => modal.classList.add('active');
    const closeModal = (modal) => modal.classList.remove('active');
    
    els.editBodyBtn.onclick = () => {
        document.getElementById('inputHeight').value = user.height || '';
        document.getElementById('inputWeight').value = user.weight || '';
        document.getElementById('inputAge').value = user.age || '';
        document.getElementById('inputSex').value = user.sex || 'Female';
        openModal(els.bodyModal);
    };
    els.logMealBtn.onclick = () => openModal(els.mealModal);
    els.closeBodyModal.onclick = () => closeModal(els.bodyModal);
    els.closeMealModal.onclick = () => closeModal(els.mealModal);

    // Fetch Initial Data
    async function init() {
        try {
            const userRes = await fetch('/api/user');
            user = await userRes.json();
            const logsRes = await fetch('/api/logs');
            logs = await logsRes.json();
            
            updateUI();
        } catch (err) {
            console.error("Failed to load data", err);
        }
    }

    // Update UI
    function updateUI() {
        if (!user.height) {
            // No user data yet
            els.heightVal.textContent = '-';
            els.weightVal.textContent = '-';
            els.ageVal.textContent = '-';
            els.sexVal.textContent = '-';
            els.bmiVal.textContent = '-';
            els.bmiStatus.textContent = 'Setup Profile';
            if(els.waterVal) els.waterVal.textContent = '-';
        } else {
            // Body Stats
            els.heightVal.textContent = user.height;
            els.weightVal.textContent = user.weight;
            els.ageVal.textContent = user.age;
            els.sexVal.textContent = user.sex;

        // BMI Calculation
        const heightM = user.height / 100;
        const bmi = (user.weight / (heightM * heightM)).toFixed(1);
        els.bmiVal.textContent = bmi;

        let statusText = "Balanced";
        let statusColor = "var(--accent-green)";
        let position = 50; // slider position percentage

        if (bmi < 18.5) {
            statusText = "Underweight";
            statusColor = "#FBBF24";
            position = (bmi / 18.5) * 18.5; // Scale to 0-18.5% region
        } else if (bmi >= 18.5 && bmi < 25) {
            statusText = "Balanced";
            statusColor = "var(--accent-green)";
            position = 18.5 + ((bmi - 18.5) / 6.5) * 31.5; // Scale to 18.5-50% region
        } else if (bmi >= 25 && bmi < 30) {
            statusText = "Overweight";
            statusColor = "#F87171";
            position = 50 + ((bmi - 25) / 5) * 20; // Scale to 50-70% region
        } else {
            statusText = "Obese";
            statusColor = "#EF4444";
            position = 70 + Math.min(((bmi - 30) / 10) * 30, 30); // Scale to 70-100% region
        }

        els.bmiStatus.textContent = statusText;
        els.bmiStatus.style.color = statusColor;
        els.bmiThumb.style.left = `${position}%`;

        // Water Goal calculation (based on BMI & weight)
        let waterLiters = (user.weight * 35) / 1000;
        if (bmi >= 25) {
            waterLiters += 0.5; // Adjust for higher BMI
        }
        if(els.waterVal) els.waterVal.textContent = waterLiters.toFixed(1);

        // Calorie Requirements (Mifflin-St Jeor)
        let bmr = (10 * user.weight) + (6.25 * user.height) - (5 * user.age);
        bmr += (user.sex === 'Male') ? 5 : -161;
        targets.calories = Math.round(bmr * 1.375); // Lightly active multiplier

        // Macros (50% carbs, 20% protein, 30% fat)
        targets.carbs = Math.round((targets.calories * 0.5) / 4);
        targets.protein = Math.round((targets.calories * 0.2) / 4);
        targets.fat = Math.round((targets.calories * 0.3) / 9);

        els.dailyTargetKcal.textContent = targets.calories;
        els.caloriesTotal.textContent = targets.calories;
        }

        // Calculate Intake
        let eaten = { calories: 0, carbs: 0, protein: 0, fat: 0 };
        logs.forEach(log => {
            eaten.calories += log.calories || 0;
            eaten.carbs += log.carbs || 0;
            eaten.protein += log.protein || 0;
            eaten.fat += log.fat || 0;
        });

        // Update Donut
        els.caloriesEaten.textContent = eaten.calories;
        const left = targets.calories - eaten.calories;
        
        if (left > 0) {
            els.caloriesLeft.textContent = `${left} kcal left`;
            els.caloriesLeft.style.color = "var(--accent-green)";
        } else {
            els.caloriesLeft.textContent = `${Math.abs(left)} kcal over`;
            els.caloriesLeft.style.color = "#F87171";
        }

        let deg = Math.min((eaten.calories / targets.calories) * 360, 360);
        let strokeColor = left < 0 ? '#F87171' : 'var(--accent-green)';
        els.calorieDonut.style.background = `conic-gradient(${strokeColor} ${deg}deg, var(--border-color) ${deg}deg)`;

        // Update Macro Bars
        const updateBar = (eatenVal, targetVal, labelEl, progressEl, suffix="g") => {
            labelEl.textContent = `${eatenVal} / ${targetVal}${suffix}`;
            const pct = Math.min((eatenVal / targetVal) * 100, 100);
            progressEl.style.width = `${pct}%`;
            if (eatenVal > targetVal) progressEl.style.backgroundColor = "#F87171";
            else progressEl.style.backgroundColor = "#E6D5C3";
        };

        updateBar(eaten.carbs, targets.carbs, els.carbsLabel, els.carbsProgress);
        updateBar(eaten.protein, targets.protein, els.proteinLabel, els.proteinProgress);
        updateBar(eaten.fat, targets.fat, els.fatLabel, els.fatProgress);

        // Update Journal List
        if (logs.length > 0) {
            els.emptyJournal.style.display = 'none';
            els.logList.innerHTML = logs.map(log => `
                <div class="log-item">
                    <div>
                        <div class="log-name">${log.name}</div>
                        <div style="font-size: 0.8rem; color: var(--text-secondary)">
                            C: ${log.carbs}g | P: ${log.protein}g | F: ${log.fat}g
                        </div>
                    </div>
                    <div style="display:flex; align-items:center; gap: 1rem;">
                        <div class="log-cals">${log.calories} <span style="font-size:0.8rem; font-family:var(--font-body)">kcal</span></div>
                        <button class="delete-btn" onclick="deleteLog(${log.id})">&times;</button>
                    </div>
                </div>
            `).join('');
        } else {
            els.emptyJournal.style.display = 'block';
            els.logList.innerHTML = '';
        }

        // Update Guidance
        updateGuidance(eaten, left);
    }

    function updateGuidance(eaten, left) {
        let guidance = [];
        
        if (left > 0) {
            guidance.push(`You have ${left} kcal remaining — enjoy a wholesome, balanced meal.`);
        } else {
            guidance.push(`You've reached your energy target for today. Focus on hydration.`);
        }

        // Macro hints
        if (targets.protein - eaten.protein > 20) {
            guidance.push("Add lean protein: grilled chicken, tofu, Greek yogurt or eggs.");
        }
        if (targets.carbs - eaten.carbs > 40) {
            guidance.push("Bring in slow carbs: oats, quinoa, brown rice or sweet potato.");
        }
        if (targets.fat - eaten.fat > 15) {
            guidance.push("Add healthy fats: avocado, almonds, olive oil or salmon.");
        }

        if (guidance.length === 1 && left > 0) {
            guidance.push("Your macros are looking wonderfully balanced today.");
        }

        els.guidanceList.innerHTML = guidance.map((text, i) => `
            <li>
                <span class="number">${i + 1}</span>
                <p>${text}</p>
            </li>
        `).join('');
    }

    // Submit Forms
    els.bodyForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const newData = {
            height: Number(document.getElementById('inputHeight').value),
            weight: Number(document.getElementById('inputWeight').value),
            age: Number(document.getElementById('inputAge').value),
            sex: document.getElementById('inputSex').value
        };

        const res = await fetch('/api/user', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(newData)
        });
        user = await res.json();
        updateUI();
        closeModal(els.bodyModal);
    });

    els.mealForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const newLog = {
            name: document.getElementById('foodName').value,
            calories: Number(document.getElementById('foodCalories').value) || 0,
            carbs: Number(document.getElementById('foodCarbs').value) || 0,
            protein: Number(document.getElementById('foodProtein').value) || 0,
            fat: Number(document.getElementById('foodFat').value) || 0
        };

        const res = await fetch('/api/logs', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(newLog)
        });
        const log = await res.json();
        logs.push(log);
        updateUI();
        els.mealForm.reset();
        closeModal(els.mealModal);
    });

    window.deleteLog = async function(id) {
        if (!confirm('Remove this food item?')) return;
        try {
            await fetch(`/api/logs/${id}`, { method: 'DELETE' });
            logs = logs.filter(l => l.id !== id);
            updateUI();
        } catch (err) {
            console.error("Failed to delete log", err);
        }
    };

    init();
});
