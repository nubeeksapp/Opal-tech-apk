-- =========================================================================================
-- OPAL TECH: SYNC HARDCODED ARTICLES INTO SUPABASE DATABASE & ADMIN PANEL
-- =========================================================================================
-- How to use:
-- 1. Open your Supabase Project Dashboard (https://supabase.com/dashboard)
-- 2. Go to the "SQL Editor" tab on the left
-- 3. Paste and run this script
-- 
-- Result:
-- All 10 bundled articles will be populated in your "posts" table.
-- They will immediately appear in your Admin Panel where you can edit, update, or manage them!
-- =========================================================================================

-- Step 1: Ensure categories and posts tables exist with all required columns
CREATE TABLE IF NOT EXISTS public.categories (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    "order" INTEGER DEFAULT 0,
    description TEXT,
    icon_emoji TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

ALTER TABLE public.categories ADD COLUMN IF NOT EXISTS "order" INTEGER DEFAULT 0;
ALTER TABLE public.categories ADD COLUMN IF NOT EXISTS description TEXT;
ALTER TABLE public.categories ADD COLUMN IF NOT EXISTS icon_emoji TEXT;

CREATE TABLE IF NOT EXISTS public.posts (
    id TEXT PRIMARY KEY,
    title TEXT NOT NULL,
    summary TEXT,
    content TEXT,
    thumbnail_url TEXT,
    category_id TEXT,
    publish_date TIMESTAMPTZ DEFAULT NOW(),
    play_store_url TEXT DEFAULT '',
    target_audience JSONB DEFAULT '[]'::jsonb,
    key_features JSONB DEFAULT '[]'::jsonb,
    prompt TEXT DEFAULT '',
    generate_prompt TEXT DEFAULT '',
    generate_prompt_enabled BOOLEAN DEFAULT false,
    scene_count INTEGER DEFAULT 0,
    scenes JSONB DEFAULT '[]'::jsonb,
    view_count BIGINT DEFAULT 0,
    is_published BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Ensure all columns exist on the posts table if it was created earlier with fewer columns
ALTER TABLE public.posts ADD COLUMN IF NOT EXISTS summary TEXT DEFAULT '';
ALTER TABLE public.posts ADD COLUMN IF NOT EXISTS content TEXT DEFAULT '';
ALTER TABLE public.posts ADD COLUMN IF NOT EXISTS thumbnail_url TEXT DEFAULT '';
ALTER TABLE public.posts ADD COLUMN IF NOT EXISTS category_id TEXT;
ALTER TABLE public.posts ADD COLUMN IF NOT EXISTS publish_date TIMESTAMPTZ DEFAULT NOW();
ALTER TABLE public.posts ADD COLUMN IF NOT EXISTS play_store_url TEXT DEFAULT '';
ALTER TABLE public.posts ADD COLUMN IF NOT EXISTS target_audience JSONB DEFAULT '[]'::jsonb;
ALTER TABLE public.posts ADD COLUMN IF NOT EXISTS key_features JSONB DEFAULT '[]'::jsonb;
ALTER TABLE public.posts ADD COLUMN IF NOT EXISTS prompt TEXT DEFAULT '';
ALTER TABLE public.posts ADD COLUMN IF NOT EXISTS generate_prompt TEXT DEFAULT '';
ALTER TABLE public.posts ADD COLUMN IF NOT EXISTS generate_prompt_enabled BOOLEAN DEFAULT false;
ALTER TABLE public.posts ADD COLUMN IF NOT EXISTS scene_count INTEGER DEFAULT 0;
ALTER TABLE public.posts ADD COLUMN IF NOT EXISTS scenes JSONB DEFAULT '[]'::jsonb;
ALTER TABLE public.posts ADD COLUMN IF NOT EXISTS view_count BIGINT DEFAULT 0;
ALTER TABLE public.posts ADD COLUMN IF NOT EXISTS is_published BOOLEAN DEFAULT true;
ALTER TABLE public.posts ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ DEFAULT NOW();

-- Step 2: Ensure default categories exist
INSERT INTO public.categories (id, name, "order", description, icon_emoji)
VALUES
    ('opal_tech', 'Opal Tech', 1, 'Official Opal Tech innovations, on-device AI breakthroughs, and next-generation mobile technologies', '💎'),
    ('tech_tips', 'Tech Tips', 2, 'Handpicked daily smartphone tricks, isolated private profiles, and smart storage hygiene guides', '📱'),
    ('online_jobs', 'Online Jobs', 3, 'Verified remote work opportunities, high-paying freelancing strategies, and digital income guides', '💼'),
    ('app_review', 'App Review', 4, 'In-depth evaluations, benchmarks, and performance reviews of essential productivity and trading apps', '⭐')
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    "order" = EXCLUDED."order";

-- Step 3: Upsert all 10 bundled articles into posts table
INSERT INTO public.posts (
    id,
    title,
    summary,
    content,
    thumbnail_url,
    category_id,
    publish_date,
    play_store_url,
    target_audience,
    key_features,
    prompt,
    generate_prompt,
    generate_prompt_enabled,
    scene_count,
    scenes,
    is_published,
    view_count
) VALUES
-- 1. Gemini Nano AI
(
    'gemini-nano-ai',
    'Gemini Nano on Android: On-Device Multimodal AI Breakthrough',
    'How modern smartphone neural processing units run compact generative AI models locally for real-time transcription, summaries, and smart replies without the cloud.',
    '<p>On-device generative AI has taken a major leap forward with Gemini Nano, Google''s most efficient model designed to execute directly on mobile silicon without sending prompt data to remote cloud servers.</p><p>By tapping into modern Neural Processing Units (NPUs), Gemini Nano transcribes long voice recordings into structured outlines, generates contextual smart replies in chat apps, and identifies critical information in text documents in milliseconds.</p><p>Because all tensor calculations happen strictly on local hardware, latency is drastically minimized and user privacy is preserved by default, even in airplane mode with zero connectivity.</p>',
    'https://fabjwphsotffrmofwksr.supabase.co/storage/v1/object/public/thumbnails/gemini_nano.png',
    'opal_tech',
    '2026-10-14T15:20:00Z',
    'https://play.google.com/store/apps/details?id=com.google.android.apps.bard',
    '["Tech enthusiasts eager to explore generative AI without cloud data privacy risks.", "Students and journalists transcribing interviews and lectures on the move.", "Frequent travelers who require AI assistance without cellular data access."]'::jsonb,
    '["Zero-latency neural processing directly on local device NPU.", "Fully functional offline AI summaries and voice memo transcriptions.", "Hardware-level privacy with no cloud telemetry or prompt logging."]'::jsonb,
    'Act as an on-device AI systems engineer.\nPart 1: Analyze hardware constraints for running quantized 4-bit neural models on mobile NPUs.\nPart 2: Evaluate memory bandwidth overhead, thermal throttling mitigation, and battery draw profiles.\nPart 3: Define an automated offline fallback pipeline when the neural accelerator is under heavy load.',
    'Act as an on-device AI systems engineer.\nPart 1: Analyze hardware constraints for running quantized 4-bit neural models on mobile NPUs.\nPart 2: Evaluate memory bandwidth overhead, thermal throttling mitigation, and battery draw profiles.\nPart 3: Define an automated offline fallback pipeline when the neural accelerator is under heavy load.',
    true,
    3,
    '["Analyze hardware constraints for running quantized 4-bit neural models on mobile NPUs.", "Evaluate memory bandwidth overhead, thermal throttling mitigation, and battery draw profiles.", "Define an automated offline fallback pipeline when the neural accelerator is under heavy load."]'::jsonb,
    true,
    240
),

-- 2. Passkeys Mobile Security
(
    'passkeys-mobile-security',
    'Passkeys on Mobile: Phishing-Resistant Login Standard',
    'How FIDO2 biometric passkeys replace fragile passwords with hardware-backed cryptographic keys, stopping credential stuffing and phishing cold.',
    '<p>Passwords are the weakest link in digital account security. Passkeys represent a fundamental shift toward cryptographic authentication anchored to your device''s secure enclave hardware.</p><p>Using asymmetric public-key cryptography, a passkey consists of a private key stored strictly in your phone''s secure element and a public key registered with the online service. Even if a website is compromised, your private key can never be leaked.</p><p>Logging in becomes as simple and fast as scanning your fingerprint or face, fully neutralizing phishing websites because passkeys are cryptographically bound to the service''s exact domain name.</p>',
    'https://fabjwphsotffrmofwksr.supabase.co/storage/v1/object/public/thumbnails/passkeys.png',
    'opal_tech',
    '2026-10-12T14:10:00Z',
    'https://play.google.com/store/apps/details?id=com.google.android.gms',
    '["Smartphone users looking to eliminate forgotten passwords and password resets.", "Security-conscious professionals protecting banking, crypto, and work accounts.", "Families looking for secure cross-device credential sharing without insecure text files."]'::jsonb,
    '["Hardware-backed cryptographic authentication via device secure enclave.", "Zero risk of credential stuffing, phishing, or server-side password leaks.", "Instant biometric login using device fingerprint or facial recognition."]'::jsonb,
    '',
    '',
    false,
    0,
    '[]'::jsonb,
    true,
    180
),

-- 3. Wi-Fi 7 Explained
(
    'wifi7-ultra-low-latency',
    'Wi-Fi 7 Explained: Next-Gen Speeds & Multi-Link Reliability',
    'How 320 MHz channels and Multi-Link Operation deliver multi-gigabit wireless bandwidth and sub-5ms latency for mobile gaming and streaming.',
    '<p>The rollout of Wi-Fi 7 (802.11be) marks a massive generational upgrade for smartphones, tablets, and mobile devices.</p><p>Multi-Link Operation (MLO) enables devices to transmit data across 5 GHz and 6 GHz simultaneously, drastically reducing latency spikes and eliminating packet drops in busy households.</p><p>With bandwidth exceeding 30 Gbps theoretically and real-world gigabit mobile connections, downloading huge files or streaming 8K video is completely seamless.</p>',
    'https://fabjwphsotffrmofwksr.supabase.co/storage/v1/object/public/thumbnails/wifi7.png',
    'opal_tech',
    '2026-10-16T12:00:00Z',
    'https://play.google.com/store/apps/details?id=net.techet.netanalyzerlite',
    '["Mobile gamers needing sub-5ms ping times without Ethernet cables.", "Remote workers transferring massive video and code repositories.", "Tech enthusiasts upgrading to next-generation routers and mesh gear."]'::jsonb,
    '["Simultaneous Multi-Link Operation across 2.4, 5, and 6 GHz spectrums.", "Ultra-wide 320 MHz channels for multi-gigabit throughput.", "High-density 4096-QAM modulation for signal clarity in crowded areas."]'::jsonb,
    '',
    '',
    false,
    0,
    '[]'::jsonb,
    true,
    310
),

-- 4. Android 15 Private Space
(
    'android-private-space',
    'Android 15 Private Space: Setup Guide & Privacy Tips',
    'Create an isolated, biometric-locked profile on your phone to hide banking apps, private documents, and confidential media from prying eyes.',
    '<p>Android 15 introduces Private Space, an OS-level sandbox that functions like a digital safe inside your device. When locked, apps inside Private Space are completely hidden from the launcher, recents screen, notifications, and settings.</p><p>You can assign a separate Google Account and a dedicated biometric fingerprint or PIN to your Private Space. This prevents sensitive work communications, cryptocurrency wallets, or banking tools from appearing in ordinary app searches.</p><p>Notifications from apps in Private Space are concealed behind privacy shields until unlocked, ensuring confidential alerts stay hidden even if someone borrows your unlocked device.</p>',
    'https://fabjwphsotffrmofwksr.supabase.co/storage/v1/object/public/thumbnails/private_space.png',
    'tech_tips',
    '2026-10-15T09:30:00Z',
    'https://play.google.com/store/apps/details?id=com.google.android.gms',
    '["Professionals who carry sensitive work documents on personal devices.", "Users who share their phone with family members or friends.", "Anyone looking for hardware-isolated app security without third-party app lockers."]'::jsonb,
    '["Independent OS-level profile with dedicated biometric unlock.", "Complete invisibility from app drawer and recent tasks when locked.", "Isolated notification channel with zero lockscreen data leaks."]'::jsonb,
    '',
    '',
    false,
    0,
    '[]'::jsonb,
    true,
    520
),

-- 5. Smart Phone Storage Hygiene
(
    'smart-storage-hygiene',
    'Smart Phone Storage Hygiene: Reclaim Space Safely',
    'Master modern storage cleaners and automated cache management to identify duplicate photos, prune hidden downloads, and free up gigabytes.',
    '<p>Running out of smartphone storage causes sluggish system animations, app crashes, and failed camera recordings. Routine digital cleaning helps you recover up to 20 GB of dormant storage.</p><p>Modern storage managers intelligently scan for burst photo duplicates, old meme downloads, oversized chat attachments, and unused app caches without risking user-saved files or critical credentials.</p><p>Setting up automated archive policies moves rarely opened games and apps into compressed standby states while keeping your personal progress and settings intact.</p>',
    'https://fabjwphsotffrmofwksr.supabase.co/storage/v1/object/public/thumbnails/storage.png',
    'tech_tips',
    '2026-10-13T11:45:00Z',
    'https://play.google.com/store/apps/details?id=com.google.android.apps.nbu.files',
    '["Users battling persistent low storage warnings.", "Mobile photographers and content creators capturing high-bitrate 4K footage.", "Anyone looking to speed up app loading times and system responsiveness."]'::jsonb,
    '["Intelligent duplicate media and blurry photo scanner.", "Safe app cache purge with zero risk of account sign-outs.", "Automated cloud backup suggestions for heavy video archives."]'::jsonb,
    '',
    '',
    false,
    0,
    '[]'::jsonb,
    true,
    410
),

-- 6. eSIM Plus
(
    'esim-plus',
    'eSIM Plus: Travel Data & Second Line Setup Guide',
    'Add a secondary cellular line for international travel and remote business calls without swapping physical SIM cards.',
    '<p>International roaming fees can quickly become exorbitant. Modern eSIM apps allow you to purchase and activate regional data profiles in seconds directly from your phone.</p><p>Having a dedicated second number is ideal for work communications, temporary marketplace listings, or online identity verification, keeping your primary number confidential.</p><p>With coverage spanning over 190 destinations, you can switch local high-speed 5G networks upon landing without searching for airport SIM kiosks.</p>',
    'https://fabjwphsotffrmofwksr.supabase.co/storage/v1/object/public/thumbnails/esim_thumb.png',
    'tech_tips',
    '2026-10-10T08:15:00Z',
    'https://play.google.com/store/apps/details?id=com.esimplus.virtualsim',
    '["Frequent international travelers and digital nomads.", "Remote workers requiring a separate business phone line.", "Users desiring phone number privacy for online transactions."]'::jsonb,
    '["Instant digital QR activation in over 190 countries.", "Dual SIM capability without removing physical plastic SIMs.", "Flexible prepaid data plans with zero roaming contract surprises."]'::jsonb,
    '',
    '',
    false,
    0,
    '[]'::jsonb,
    true,
    1420
),

-- 7. Freelancing in 2026
(
    'freelance-digital-jobs',
    'Freelancing in 2026: Next-Gen Online Career & Remote Strategies',
    'Master top global freelancing platforms, digital escrow payments, and remote client acquisition tools to earn consistently online.',
    '<p>The global remote freelance economy has transitioned into specialized high-value knowledge services. Building a resilient independent career begins with identifying verifiable technical skills.</p><p>From mobile development and copyediting to cloud bookkeeping and UI design, established platforms like Upwork and Fiverr connect global talent directly with verified corporate clients.</p><p>Utilizing digital escrow arrangements ensures that contract milestones are funded before work begins, eliminating payment disputes and providing predictable international wire transfers.</p>',
    'https://fabjwphsotffrmofwksr.supabase.co/storage/v1/object/public/thumbnails/freelance.png',
    'online_jobs',
    '2026-10-18T10:30:00Z',
    'https://play.google.com/store/apps/details?id=com.upwork.android.apps.main',
    '["Professionals seeking location-independent remote freelance income.", "Tech specialists and writers wanting to monetize specialized expertise.", "Students and graduates looking to build an international work portfolio."]'::jsonb,
    '["Verified payment protection via secure automated milestone escrows.", "Global client matching algorithms based on skill badges and reviews.", "Direct mobile job proposals, contract signing, and invoice management."]'::jsonb,
    '',
    '',
    false,
    0,
    '[]'::jsonb,
    true,
    640
),

-- 8. Passive Income Online
(
    'passive-income-online',
    'How to Start Working Online & Earn Passive Revenue',
    'Explore high-paying online roles from technical translation and AI prompt curation to digital product creation without startup capital.',
    '<p>Starting an online career no longer requires heavy initial capital. Multilingual translation, data annotation, digital template publishing, and virtual assistant roles can be performed entirely from a smartphone or laptop.</p><p>Passive revenue systems involve creating digital assets once—such as specialized Notion templates, graphic vector packs, or technical tutorials—and distributing them through automated marketplaces.</p><p>Consistency, timely client communication, and positive early reviews compound your profile visibility, establishing steady recurring revenue streams over time.</p>',
    'https://fabjwphsotffrmofwksr.supabase.co/storage/v1/object/public/thumbnails/passive_income.png',
    'online_jobs',
    '2026-10-17T14:00:00Z',
    'https://play.google.com/store/apps/details?id=com.fiverr.fiverr',
    '["Individuals exploring work-from-home employment opportunities.", "Bilingual speakers able to offer live translation and transcription services.", "Digital creators interested in scalable passive digital product income."]'::jsonb,
    '["Low barrier to entry with zero upfront hardware costs required.", "Multiple revenue streams across micro-tasks and recurring retainers.", "Instant smartphone notifications for incoming orders and customer inquiries."]'::jsonb,
    '',
    '',
    false,
    0,
    '[]'::jsonb,
    true,
    490
),

-- 9. Mobile Scanner PDF Workflow
(
    'mobile-scanner-pdf-workflow',
    'Paperless Office: Mobile Document Scanners & Cloud OCR',
    'Convert multi-page contracts, paper receipts, and handwritten notes into searchable PDFs with automated deskewing and instant cloud synchronization.',
    '<p>Modern smartphone cameras paired with machine learning algorithms have rendered bulky flatbed scanners obsolete. Real-time edge detection instantly snaps documents, straightens perspective distortion, and eliminates table shadows.</p><p>Built-in optical character recognition (OCR) turns printed text into searchable, selectable copy. You can copy invoice numbers, search contracts by client name, or export text directly into office editors.</p><p>Seamless integrations automatically sync finalized PDFs directly into Google Drive, OneDrive, or company shared drives, keeping your digital paperwork organized wherever work takes you.</p>',
    'https://fabjwphsotffrmofwksr.supabase.co/storage/v1/object/public/thumbnails/scanner.png',
    'app_review',
    '2026-10-11T10:00:00Z',
    'https://play.google.com/store/apps/details?id=com.adobe.scan.android',
    '["Remote workers and freelancers handling vendor agreements and expense receipts.", "Students digitizing study guides, whiteboards, and research papers.", "Small business owners striving for a fast, paperless administrative workflow."]'::jsonb,
    '["Automated edge detection, glare reduction, and perspective correction.", "High-accuracy multilingual OCR for instant text search within PDFs.", "Direct cloud drive export with password protection and digital signing."]'::jsonb,
    '',
    '',
    false,
    0,
    '[]'::jsonb,
    true,
    380
),

-- 10. MetaTrader App Review
(
    'metatrader-app-review',
    'App Review: MetaTrader 4 & Mobile Financial Trading',
    'An in-depth performance, security, and interface review of MetaTrader 4 (MT4) for mobile trading, chart telemetry, and automated analysis.',
    '<p>MetaTrader 4 (MT4) remains the industry benchmark platform for mobile Forex and financial market analysis, connecting traders directly with licensed financial brokers globally.</p><p>The Android app provides interactive real-time price quotes, technical charting with 30 built-in indicators, and instant order execution with complete stop-loss and take-profit risk management.</p><p>Crucially, MetaQuotes operates strictly as software platform provider rather than a financial brokerage, ensuring users retain complete control over their choice of broker and personal funds.</p>',
    'https://fabjwphsotffrmofwksr.supabase.co/storage/v1/object/public/thumbnails/metatrader.png',
    'app_review',
    '2026-10-09T16:45:00Z',
    'https://play.google.com/store/apps/details?id=net.metaquotes.metatrader4',
    '["Financial market analysts and active currency traders on mobile.", "Economics students studying technical chart indicators and market depth.", "Users requiring encrypted order transmission on portable devices."]'::jsonb,
    '["Comprehensive technical analysis suite with 30 technical indicators.", "Real-time interactive candlestick and bar charts with zoom and scroll.", "Complete financial calendar, live news feed, and automated trade alerts."]'::jsonb,
    '',
    '',
    false,
    0,
    '[]'::jsonb,
    true,
    890
)

ON CONFLICT (id) DO UPDATE SET
    title = EXCLUDED.title,
    summary = EXCLUDED.summary,
    content = EXCLUDED.content,
    thumbnail_url = EXCLUDED.thumbnail_url,
    category_id = EXCLUDED.category_id,
    play_store_url = EXCLUDED.play_store_url,
    target_audience = EXCLUDED.target_audience,
    key_features = EXCLUDED.key_features,
    prompt = EXCLUDED.prompt,
    generate_prompt = EXCLUDED.generate_prompt,
    generate_prompt_enabled = EXCLUDED.generate_prompt_enabled,
    scene_count = EXCLUDED.scene_count,
    scenes = EXCLUDED.scenes,
    is_published = EXCLUDED.is_published;

-- Step 4: Create view 'articles' so both 'posts' and 'articles' queries work
CREATE OR REPLACE VIEW public.articles AS SELECT * FROM public.posts;

-- Step 5: Ensure Row Level Security allows reading and writing
ALTER TABLE public.categories ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.posts ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Public read access for categories" ON public.categories;
CREATE POLICY "Public read access for categories"
    ON public.categories FOR SELECT TO anon, authenticated USING (true);

DROP POLICY IF EXISTS "Admin write access for categories" ON public.categories;
CREATE POLICY "Admin write access for categories"
    ON public.categories FOR ALL TO anon, authenticated USING (true) WITH CHECK (true);

DROP POLICY IF EXISTS "Public read access for posts" ON public.posts;
CREATE POLICY "Public read access for posts"
    ON public.posts FOR SELECT TO anon, authenticated USING (true);

DROP POLICY IF EXISTS "Admin write access for posts" ON public.posts;
CREATE POLICY "Admin write access for posts"
    ON public.posts FOR ALL TO anon, authenticated USING (true) WITH CHECK (true);

