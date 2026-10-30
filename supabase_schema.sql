-- Opal Tech: Supabase Database Schema & Setup Script
-- Run this script in the Supabase Dashboard -> SQL Editor to initialize your tables, storage, and seed articles.

-- 1. Create categories table
CREATE TABLE IF NOT EXISTS public.categories (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    "order" INTEGER DEFAULT 0,
    description TEXT,
    icon_emoji TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 2. Create posts table (with all content, thumbnail, audience, and key features fields)
CREATE TABLE IF NOT EXISTS public.posts (
    id TEXT PRIMARY KEY,
    title TEXT NOT NULL,
    summary TEXT,
    content TEXT,
    thumbnail_url TEXT,
    category_id TEXT REFERENCES public.categories(id) ON DELETE SET NULL,
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

-- Add any missing columns if table already existed previously
ALTER TABLE public.posts ADD COLUMN IF NOT EXISTS target_audience JSONB DEFAULT '[]'::jsonb;
ALTER TABLE public.posts ADD COLUMN IF NOT EXISTS key_features JSONB DEFAULT '[]'::jsonb;
ALTER TABLE public.posts ADD COLUMN IF NOT EXISTS prompt TEXT DEFAULT '';
ALTER TABLE public.posts ADD COLUMN IF NOT EXISTS generate_prompt TEXT DEFAULT '';
ALTER TABLE public.posts ADD COLUMN IF NOT EXISTS generate_prompt_enabled BOOLEAN DEFAULT false;
ALTER TABLE public.posts ADD COLUMN IF NOT EXISTS scene_count INTEGER DEFAULT 0;
ALTER TABLE public.posts ADD COLUMN IF NOT EXISTS scenes JSONB DEFAULT '[]'::jsonb;
ALTER TABLE public.posts ADD COLUMN IF NOT EXISTS view_count BIGINT DEFAULT 0;
ALTER TABLE public.posts ADD COLUMN IF NOT EXISTS is_published BOOLEAN DEFAULT true;
ALTER TABLE public.posts ADD COLUMN IF NOT EXISTS play_store_url TEXT DEFAULT '';
ALTER TABLE public.posts ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ DEFAULT NOW();

-- Create a view 'articles' so admin panels using either 'posts' or 'articles' table name work seamlessly
CREATE OR REPLACE VIEW public.articles AS SELECT * FROM public.posts;

-- 3. Stored procedure / RPC to increment article view count atomically
CREATE OR REPLACE FUNCTION public.increment_view_count(p_article_id TEXT)
RETURNS VOID AS $$
BEGIN
    UPDATE public.posts
    SET view_count = COALESCE(view_count, 0) + 1
    WHERE id = p_article_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Grant execution permissions on the RPC to anon and authenticated roles
GRANT EXECUTE ON FUNCTION public.increment_view_count(TEXT) TO anon, authenticated;

-- 4. Enable Row Level Security (RLS) on tables
ALTER TABLE public.categories ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.posts ENABLE ROW LEVEL SECURITY;

-- Categories RLS: Read access for everyone, Write access for admin panel / management
DROP POLICY IF EXISTS "Public read access for categories" ON public.categories;
CREATE POLICY "Public read access for categories"
    ON public.categories FOR SELECT TO anon, authenticated USING (true);

DROP POLICY IF EXISTS "Admin write access for categories" ON public.categories;
CREATE POLICY "Admin write access for categories"
    ON public.categories FOR ALL TO anon, authenticated USING (true) WITH CHECK (true);

-- Posts RLS: Read access for everyone, Write access for admin panel / management
DROP POLICY IF EXISTS "Public read access for posts" ON public.posts;
CREATE POLICY "Public read access for posts"
    ON public.posts FOR SELECT TO anon, authenticated USING (true);

DROP POLICY IF EXISTS "Admin write access for posts" ON public.posts;
CREATE POLICY "Admin write access for posts"
    ON public.posts FOR ALL TO anon, authenticated USING (true) WITH CHECK (true);

-- 5. Storage bucket for thumbnails
INSERT INTO storage.buckets (id, name, public)
VALUES ('thumbnails', 'thumbnails', true)
ON CONFLICT (id) DO UPDATE SET public = true;

-- Storage RLS: Public read, upload & update access
DROP POLICY IF EXISTS "Public read thumbnails" ON storage.objects;
CREATE POLICY "Public read thumbnails"
    ON storage.objects FOR SELECT TO anon, authenticated
    USING (bucket_id = 'thumbnails');

DROP POLICY IF EXISTS "Allow uploads to thumbnails" ON storage.objects;
CREATE POLICY "Allow uploads to thumbnails"
    ON storage.objects FOR INSERT TO anon, authenticated
    WITH CHECK (bucket_id = 'thumbnails');

DROP POLICY IF EXISTS "Allow updates to thumbnails" ON storage.objects;
CREATE POLICY "Allow updates to thumbnails"
    ON storage.objects FOR UPDATE TO anon, authenticated
    USING (bucket_id = 'thumbnails');

-- 6. Seed initial categories (The 4 Official Opal Tech Categories)
INSERT INTO public.categories (id, name, "order", description, icon_emoji)
VALUES
    ('opal_tech', 'Opal Tech', 1, 'Official Opal Tech innovations, on-device AI breakthroughs, and next-generation mobile technologies', '💎'),
    ('tech_tips', 'Tech Tips', 2, 'Handpicked daily smartphone tricks, isolated private profiles, and smart storage hygiene guides', '📱'),
    ('online_jobs', 'Online Jobs', 3, 'Verified remote work opportunities, high-paying freelancing strategies, and digital income guides', '💼'),
    ('app_review', 'App Review', 4, 'In-depth evaluations, benchmarks, and performance reviews of essential productivity and trading apps', '⭐')
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    "order" = EXCLUDED."order",
    description = EXCLUDED.description,
    icon_emoji = EXCLUDED.icon_emoji;

-- 7. Seed initial posts
INSERT INTO public.posts (id, title, summary, content, thumbnail_url, category_id, publish_date, play_store_url, target_audience, key_features, view_count, is_published)
VALUES
    (
        'esim-plus',
        'eSIM Plus — Travel Data & 2nd line in One App',
        'Add a 2nd line for travel and work. US phone numbers with no SIM card to replace.',
        'Need a second phone number before a trip? Want US phone numbers for online services, work contacts or travel? Looking for eSIM for travel while abroad? eSIM Plus helps you set up the right connection quickly, with no SIM card to replace.

You can add a 2nd number for sign-ups, projects, marketplaces, dating apps or temporary conversations. Extra numbers also help keep your main number private while you stay reachable wherever you go.

With global cellular coverage in over 190 countries, you can switch between regional and international data plans without incurring high roaming fees from your home carrier.',
        'https://fabjwphsotffrmofwksr.supabase.co/storage/v1/object/public/thumbnails/esim_thumb.png',
        'tech_tips',
        NOW() - INTERVAL '1 day',
        'https://play.google.com/store/apps/details?id=com.esimplus.virtualsim',
        '["Travellers who want reliable mobile data abroad.", "Remote workers who need flexible mobile access.", "Users who want a second phone number for privacy."]'::jsonb,
        '["Instant digital activation in 190+ countries.", "No physical SIM swapping required.", "Zero roaming fee surprises on trip."]'::jsonb,
        1420,
        true
    ),
    (
        'secure-proxy',
        'Secure Proxy — Encrypted Mobile Privacy & Fast Shield',
        'Fast and private encrypted VPN proxy for open WiFi hotspots and streaming protection.',
        'Unprotected public Wi-Fi networks in airports, hotels, and cafes leave your personal credentials vulnerable to snooping. Secure Proxy creates a robust, encrypted tunnel for your mobile traffic.

Powered by modern WireGuard and OpenVPN protocols, the app ensures minimal battery consumption while delivering speeds exceeding 800 Mbps. Enjoy unlimited bandwidth with zero speed throttling.

Strict zero-logging architecture ensures your browsing queries, download history, and IP address remain completely anonymous and never recorded to disk.',
        'https://fabjwphsotffrmofwksr.supabase.co/storage/v1/object/public/thumbnails/proxy_thumb.png',
        'opal_tech',
        NOW() - INTERVAL '2 days',
        '',
        '["Remote employees working on open public coffee shop WiFi.", "Streaming enthusiasts wanting buffer-free access.", "Everyday users wishing to shield browsing history."]'::jsonb,
        '["WireGuard high-speed encryption.", "Strict zero logs policy.", "One-tap connection to 50+ global locations."]'::jsonb,
        980,
        true
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
    view_count = EXCLUDED.view_count,
    is_published = EXCLUDED.is_published;
