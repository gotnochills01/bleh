CREATE DATABASE IF NOT EXISTS mahila_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE mahila_db;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS health_profile (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    age INT NOT NULL,
    date_of_birth DATE,
    weight_kg DECIMAL(5,2),
    height_cm DECIMAL(5,2),
    blood_group VARCHAR(5),
    city VARCHAR(100),
    emergency_medical_notes TEXT,
    average_cycle_length INT DEFAULT 28,
    average_period_length INT DEFAULT 5,
    preferred_language VARCHAR(10) DEFAULT 'en',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS periods (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    flow_level VARCHAR(20) DEFAULT 'MEDIUM',
    pain_scale INT CHECK (pain_scale BETWEEN 0 AND 10),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_periods_user_date (user_id, start_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS symptoms (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    symptom_name VARCHAR(100) NOT NULL,
    severity VARCHAR(20) DEFAULT 'MODERATE',
    logged_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_symptoms_user_date (user_id, logged_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS moods (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    mood_type VARCHAR(20) NOT NULL,
    intensity INT DEFAULT 3 CHECK (intensity BETWEEN 1 AND 5),
    logged_date DATE NOT NULL,
    notes VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_moods_user_date (user_id, logged_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS contacts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    contact_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    category VARCHAR(30) NOT NULL DEFAULT 'OTHER',
    is_favorite BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_contacts_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS appointments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    doctor_name VARCHAR(100) NOT NULL,
    specialization VARCHAR(100),
    clinic_or_hospital VARCHAR(150),
    appointment_date_time DATETIME NOT NULL,
    is_completed BOOLEAN DEFAULT FALSE,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_appointments_user (user_id, appointment_date_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS reminders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    reminder_type VARCHAR(30) NOT NULL,
    title VARCHAR(150) NOT NULL,
    reminder_time TIME NOT NULL,
    repeat_days VARCHAR(50),
    is_enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_reminders_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS medications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    medication_name VARCHAR(100) NOT NULL,
    dosage VARCHAR(50),
    frequency VARCHAR(50),
    start_date DATE,
    end_date DATE,
    notes VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_medications_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    notification_type VARCHAR(50),
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_notifications_user (user_id, is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user_quiz_progress (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category VARCHAR(50) NOT NULL,
    difficulty VARCHAR(20) NOT NULL,
    score INT NOT NULL,
    total_questions INT NOT NULL,
    completed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS nutrition_recommendations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cycle_phase VARCHAR(50) NOT NULL,
    meal_category VARCHAR(50) NOT NULL,
    food_item_name VARCHAR(150) NOT NULL,
    key_nutrients VARCHAR(255) NOT NULL,
    health_benefits TEXT,
    dietary_type VARCHAR(30) NOT NULL,
    budget_level VARCHAR(30) NOT NULL,
    evidence_level VARCHAR(50) NOT NULL,
    safety_warnings TEXT,
    evidence_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_nutrition_phase_diet (cycle_phase, dietary_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Seed evidence-based nutrition items
INSERT INTO nutrition_recommendations (cycle_phase, meal_category, food_item_name, key_nutrients, health_benefits, dietary_type, budget_level, evidence_level, safety_warnings, evidence_url)
VALUES
('MENSTRUAL', 'Main Meal', 'Spinach & Lentil Dal with Citrus', 'Non-heme Iron, Vitamin C, Folate, Fiber', 'Vitamin C enhances non-heme iron absorption to replenish menstrual blood loss and maintain energy levels.', 'VEGAN', 'BUDGET', 'WHO Dietary Guidelines', 'Consult physician if taking anticoagulant medications due to high Vitamin K content.', 'https://pubmed.ncbi.nlm.nih.gov/20200263/'),
('MENSTRUAL', 'Snack', 'Pumpkin Seeds & Dark Chocolate (70%+)', 'Magnesium, Zinc, Non-heme Iron', 'Magnesium assists in uterine muscle relaxation to help reduce mild primary dysmenorrhea symptoms.', 'VEGETARIAN', 'MODERATE', 'Systematic Review / RCT', 'Consume in moderation. High cocoa content may contain small amounts of caffeine.', 'https://pubmed.ncbi.nlm.nih.gov/23865352/'),
('FOLLICULAR', 'Main Meal', 'Sprouted Chickpea Salad with Cucumbers', 'Complex Carbohydrates, Protein, B-Vitamins', 'Provides sustained glucose release and micronutrients essential for cellular repair during follicle development.', 'VEGAN', 'BUDGET', 'National Dietary Guidelines', 'Wash sprouted legumes thoroughly to prevent foodborne bacterial contamination.', 'https://pubmed.ncbi.nlm.nih.gov/28630128/'),
('FOLLICULAR', 'Breakfast', 'Fortified Oats with Chia Seeds & Berries', 'Soluble Fiber (Beta-glucan), Omega-3 ALA, Antiox', 'Supports healthy digestion and liver estrogen metabolism during early follicular rise.', 'VEGAN', 'BUDGET', 'Clinical Review', 'Ensure adequate water consumption with high chia seed fiber intake.', 'https://pubmed.ncbi.nlm.nih.gov/22583023/'),
('OVULATORY', 'Main Meal', 'Grilled Mackerel or Salmon with Asparagus', 'Omega-3 EPA/DHA, Vitamin D, Selenium', 'Anti-inflammatory fatty acids support vascular tone and inflammatory regulation around ovulation.', 'NON_VEG', 'MODERATE', 'Systematic Review', 'Select low-mercury fish sources. Pregnant or lactating individuals should adhere to local FDA/WHO fish limits.', 'https://pubmed.ncbi.nlm.nih.gov/30206192/'),
('OVULATORY', 'Snack', 'Sesame & Flax Seed Mix (Seed Cycling Support)', 'Lignans, Essential Fatty Acids, Zinc', 'Provides plant dietary lignans and zinc supporting general hormone signaling health.', 'VEGAN', 'BUDGET', 'Emerging Clinical Cohort', 'Food supplement only. Does not replace clinical therapy for endocrine or ovulatory disorders.', 'https://pubmed.ncbi.nlm.nih.gov/21812903/'),
('LUTEAL', 'Main Meal', 'Baked Sweet Potato with Black Beans & Avocado', 'Vitamin B6, Potassium, Complex Carbs, Fiber', 'Vitamin B6 aids neurotransmitter synthesis (serotonin) reducing luteal mood shifts and water retention.', 'VEGAN', 'BUDGET', 'RCT Clinical Trial', 'High potassium foods should be monitored in individuals with chronic kidney disease.', 'https://pubmed.ncbi.nlm.nih.gov/28165863/'),
('LUTEAL', 'Snack', 'Plain Greek Yogurt with Walnuts', 'Calcium, Protein, Probiotics, Omega-3', 'Dietary calcium (1000mg/day) is clinically shown to reduce premenstrual syndrome symptom severity.', 'VEGETARIAN', 'MODERATE', 'Systematic Review / RCT', 'Not recommended for individuals with severe lactose intolerance or milk protein allergy without lactase.', 'https://pubmed.ncbi.nlm.nih.gov/28215622/'),
('GENERAL', 'Hydration / Beverage', 'Chamomile & Ginger Herbal Tea', 'Flavonoids, Gingerols', 'Chamomile and ginger infusions contain mild antispasmodic compounds easing gastrointestinal and uterine discomfort.', 'VEGAN', 'BUDGET', 'Systematic Review / RCT', 'Avoid ginger supplements in high doses prior to surgery or if taking anti-platelet therapy.', 'https://pubmed.ncbi.nlm.nih.gov/27000311/'),
('GENERAL', 'Breakfast', 'Boiled Eggs on Whole Grain Toast', 'Choline, High-Quality Protein, Vitamin B12', 'Supports cellular integrity, brain health, and steady glycemic control throughout the cycle.', 'NON_VEG', 'BUDGET', 'National Dietary Guidelines', 'Cook eggs thoroughly to lower risk of Salmonella infection.', 'https://pubmed.ncbi.nlm.nih.gov/28900017/');
