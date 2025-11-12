-- Add auth_provider column to track how users signed up
-- This helps with security, UX, and compliance

ALTER TABLE users 
ADD COLUMN auth_provider VARCHAR(20) NOT NULL DEFAULT 'LOCAL';

-- Add index for faster lookups
CREATE INDEX idx_users_auth_provider ON users(auth_provider);

-- Optional: Add comment for documentation
COMMENT ON COLUMN users.auth_provider IS 'Authentication provider: LOCAL (email/password), GITHUB (GitHub OAuth), GOOGLE (Google OAuth)';
