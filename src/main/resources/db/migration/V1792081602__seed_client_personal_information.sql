-- Keep the existing demo client profile populated with personal information only.
-- The associated account was created by V1792081606; do not alter account or document data here.
UPDATE user_module.users
SET first_name = 'Буряк',
    last_name = 'Володимир',
    middle_name = 'Сергійович',
    phone_number = '+380671234567'
WHERE identity_out_ref = 'e4c350fd-a59a-46cf-9d7d-5c0ed287feba';

UPDATE user_module.client_profiles AS profile
SET birth_date = DATE '1994-08-14'
FROM user_module.users AS account
WHERE account.id = profile.user_id
  AND account.identity_out_ref = 'e4c350fd-a59a-46cf-9d7d-5c0ed287feba';
