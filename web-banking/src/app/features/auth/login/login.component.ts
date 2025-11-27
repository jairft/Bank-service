import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { RouterModule } from '@angular/router';

import { ErrorMessageComponent } from '../../../shared/components/error-message/error-message';

// Angular Material
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatCardModule } from '@angular/material/card';

import { AuthService } from '../../../core/services/auth';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatCardModule,
    RouterModule,
    ErrorMessageComponent
  ],
  templateUrl: './login.html',
  styleUrls: ['./login.scss']
})
export class LoginComponent {
  loginForm: FormGroup;
  isLoading = false;
  errorMessage: string = '';

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.loginForm = this.formBuilder.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  onSubmit(): void {
    if (this.loginForm.valid) {
      this.isLoading = true;
      this.errorMessage = '';

      const { email, password } = this.loginForm.value;

      this.authService.login(email, password).subscribe({
        next: (res: any) => {
          this.isLoading = false;

          // 🔹 Trata o status retornado do backend
          switch (res.status) {
            case 'PENDING_ACTIVATION':
               this.errorMessage = 'Sua conta ainda não foi ativada. Você será redirecionado para ativação.';
              // redireciona após 3 segundos
              setTimeout(() => {
                this.router.navigate(['/activate-account'], { queryParams: { email } });
              }, 4000);
              break;
            case 'INACTIVE':
              this.errorMessage = 'Sua conta está desativada. Contate o suporte.';
              break;
            case 'ACTIVE':
              // usuário ativo → salvar no localStorage e navegar para dashboard
              localStorage.setItem('currentUser', JSON.stringify(res));
              this.router.navigate(['/dashboard']);
              break;
            default:
              this.errorMessage = 'Status desconhecido.';
              break;
          }
        },
        error: (err) => this.handleError(err)
      });
    } else {
      this.markFormGroupTouched();
    }
  }

  private markFormGroupTouched(): void {
    Object.keys(this.loginForm.controls).forEach((key) => {
      this.loginForm.get(key)?.markAsTouched();
    });
  }

  get email() {
    return this.loginForm.get('email');
  }

  get password() {
    return this.loginForm.get('password');
  }

  private handleError(err: any) {
    this.isLoading = false;
    if (err.error?.message) this.errorMessage = err.error.message;
    else if (typeof err.error === 'string') this.errorMessage = err.error;
    else this.errorMessage = 'Erro desconhecido. Tente novamente.';

  }
}
