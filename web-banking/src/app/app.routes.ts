import { Routes } from '@angular/router';
import { AuthGuard } from './core/guards/auth-guard';

export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },

  { 
    path: 'login', 
    loadComponent: () => import('./features/auth/login/login.component')
      .then(m => m.LoginComponent) 
  },
  { 
    path: 'register', 
    loadComponent: () => import('./features/auth/register/register.component')
      .then(m => m.RegisterComponent) 
  },
  { 
    path: 'dashboard', 
    loadComponent: () => import('./features/dashboard/dashboard.component')
      .then(m => m.DashboardComponent),
    canActivate: [AuthGuard] 
  },
  { 
    path: 'pix', 
    loadComponent: () => import('./features/pix/pix')
      .then(m => m.PixComponent),
    canActivate: [AuthGuard] 
  },
  { 
    path: 'deposit', 
    loadComponent: () => import('./features/deposit/deposit')
      .then(m => m.DepositComponent),
    canActivate: [AuthGuard] 
  },
  { 
  path: 'transaction-password', 
  loadComponent: () => import('./features/transaction-password/transaction-password')
    .then(m => m.TransactionalPasswordComponent),
  canActivate: [AuthGuard] 
},
  { 
    path: 'cards', 
    loadComponent: () => import('./features/cards/cards')
      .then(m => m.CardToolsComponent),
    canActivate: [AuthGuard] 
  },

  { 
    path: 'activate-account', 
    loadComponent: () => import('./features/auth/activate-account/activate-account')
      .then(m => m.ActivateAccountComponent)
  },
  { 
    path: 'reset-password', 
    loadComponent: () => import('./features/auth/reset/reset-password')
      .then(m => m.ResetPasswordComponent) 
},
{
    path: 'update-password',
    loadComponent: () => import('./features/auth/update-password/update-password')
      .then(m => m.UpdatePasswordComponent),
    canActivate: [AuthGuard]
  },
  {
    path: 'edit-user',
    loadComponent: () => import('./features/auth/edit-user/edit-user')
      .then(m => m.EditUserComponent),
    canActivate: [AuthGuard]
},
{
  path: 'reset-transactional-password',
  loadComponent: () => import('./features/reset-transactional-password/reset-transactional-password')
    .then(m => m.ResetTransactionalPasswordComponent)
},
{
  path: 'request-card',
  loadComponent: () => import('./features/cards/request-card/request-card')
    .then(m => m.RequestCardComponent)
},

  { path: '**', redirectTo: '/login' }
];
