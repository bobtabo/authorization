Rails.application.routes.draw do
  # OAuth（ブラウザリダイレクトのため /api 外）
  get  'auth/google/redirect', to: 'api/auth#google_redirect'
  get  'auth/google/callback',  to: 'api/auth#google_callback'

  namespace :api, defaults: { format: :json } do
    # --- auth ---
    get  'auth/me',               to: 'auth#get_my_profile'
    get  'auth/login',            to: 'auth#login'
    get  'auth/logout',           to: 'auth#logout'
    get  'auth/invitation/:token', to: 'auth#invitation'

    # --- clients（store を :id より先に定義して衝突回避）---
    get    'clients',             to: 'clients#index'
    post   'clients/store',       to: 'clients#store'
    put    'clients/:id/update',  to: 'clients#update',  constraints: { id: /\d+/ }
    get    'clients/:id',         to: 'clients#show',    constraints: { id: /\d+/ }
    delete 'clients/:id/delete',  to: 'clients#destroy', constraints: { id: /\d+/ }

    # --- staffs ---
    get    'staffs',                    to: 'staffs#index'
    patch  'staffs/:id/updateRole',     to: 'staffs#update_role',  constraints: { id: /\d+/ }
    patch  'staffs/:id/restore',        to: 'staffs#restore',      constraints: { id: /\d+/ }
    delete 'staffs/:id/delete',         to: 'staffs#destroy',      constraints: { id: /\d+/ }

    # --- admin ---
    namespace :admin do
      get 'invitation',       to: 'invitations#index'
      get 'invitation/issue', to: 'invitations#issue'
    end

    # --- gate ---
    get 'gate/issue',                       to: 'gate#issue'
    get 'gate/client/:identifier/verify',   to: 'gate#verify'

    # --- notifications ---
    get   'notifications/counts', to: 'notifications#counts'
    get   'notifications',        to: 'notifications#index'
    patch 'notifications',        to: 'notifications#read_all'
    patch 'notifications/:id',    to: 'notifications#read', constraints: { id: /\d+/ }
  end
end
