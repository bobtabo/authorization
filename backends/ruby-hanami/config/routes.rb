module Authorization
  class Routes < Hanami::Routes
    # OAuth（ブラウザリダイレクトのため /api 外）
    get '/auth/google/redirect', to: 'auth.google_redirect'
    get '/auth/google/callback',  to: 'auth.google_callback'
    get '/auth/github/redirect', to: 'auth.github_redirect'
    get '/auth/github/callback',  to: 'auth.github_callback'

    # --- auth ---
    get '/api/auth/me',               to: 'auth.me'
    get '/api/auth/login',            to: 'auth.login'
    get '/api/auth/logout',           to: 'auth.logout'
    get '/api/auth/invitation/:token', to: 'auth.invitation'

    # --- clients ---
    get    '/api/clients',                           to: 'clients.index'
    post   '/api/clients/store',                     to: 'clients.store'
    get    '/api/clients/:id/jwt-histories',         to: 'clients.jwt_histories'
    put    '/api/clients/:id/update',                to: 'clients.update'
    get    '/api/clients/:id',                       to: 'clients.show'
    delete '/api/clients/:id/delete',                to: 'clients.destroy'
    get    '/api/clients/:identifier/qr',            to: 'clients.qr'
    get    '/api/clients/:identifier/info',          to: 'clients.info'
    patch  '/api/clients/:identifier/start',         to: 'clients.start'
    patch  '/api/clients/:identifier/stop',          to: 'clients.stop'

    # --- staffs ---
    get    '/api/staffs',                   to: 'staffs.index'
    patch  '/api/staffs/:id/updateRole',    to: 'staffs.update_role'
    patch  '/api/staffs/:id/restore',       to: 'staffs.restore'
    delete '/api/staffs/:id/delete',        to: 'staffs.destroy'

    # --- admin ---
    get '/api/admin/invitation',       to: 'admin.invitations.index'
    get '/api/admin/invitation/issue', to: 'admin.invitations.issue'

    # --- gate ---
    get '/api/gate/issue',                     to: 'gate.issue'
    get '/api/gate/client/:identifier/verify', to: 'gate.verify'

    # --- notifications ---
    get   '/api/notifications/counts', to: 'notifications.counts'
    get   '/api/notifications',        to: 'notifications.index'
    patch '/api/notifications',        to: 'notifications.read_all'
    patch '/api/notifications/:id',    to: 'notifications.read'
  end
end
