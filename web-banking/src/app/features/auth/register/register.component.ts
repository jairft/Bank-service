import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';

/* Angular Material */
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatCardModule } from '@angular/material/card';



/* Ngx-Mask */
import { NgxMaskDirective, provideNgxMask } from 'ngx-mask';
import { ErrorMessageComponent } from '../../../shared/components/error-message/error-message';

@Component({
  selector: 'app-register',
  standalone: true,
  templateUrl: './register.html',
  styleUrls: ['./register.scss'],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    HttpClientModule,
    RouterModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatCardModule,
    NgxMaskDirective,
    ErrorMessageComponent
  ],
  providers: [provideNgxMask()]
})
export class RegisterComponent {
  registerForm: FormGroup;
  isLoading = false;
  errorMessage: string | null = null;

  hidePassword = true;
  hideConfirmPassword = true;

  constructor(
    private fb: FormBuilder,
    private http: HttpClient,
    private router: Router
  ) {
    this.registerForm = this.fb.group(
      {
        nome: ['', [Validators.required]],
        cpf: ['', [Validators.required]],
        email: ['', [Validators.required, Validators.email]],
        telefone: ['', [Validators.required]],
        password: ['', [Validators.required, Validators.minLength(6)]],
        confirmPassword: ['', [Validators.required]]
      },
      { validators: this.passwordMatchValidator }
    );
  }

  passwordMatchValidator(group: FormGroup) {
    const password = group.get('password')?.value;
    const confirmPassword = group.get('confirmPassword')?.value;
    return password === confirmPassword ? null : { passwordMismatch: true };
  }

  get nome() { return this.registerForm.get('nome'); }
  get cpf() { return this.registerForm.get('cpf'); }
  get email() { return this.registerForm.get('email'); }
  get telefone() { return this.registerForm.get('telefone'); }
  get password() { return this.registerForm.get('password'); }
  get confirmPassword() { return this.registerForm.get('confirmPassword'); }

  showGlobalError = false;

  onSubmit() {
    
    this.showGlobalError = true;
  // Marca todos os campos como tocados para mostrar erros
    this.registerForm.markAllAsTouched();

    // Se o formulário for inválido, mostra mensagem de erro
    if (this.registerForm.invalid) {
      if (this.registerForm.errors?.['passwordMismatch']) {
        this.errorMessage = 'As senhas não coincidem.';
      } else {
        this.errorMessage = 'Preencha todos os campos obrigatórios corretamente.';
      }
      return;
    }

    this.isLoading = true;
    this.errorMessage = null;

    const formValue = { ...this.registerForm.value };
    formValue.cpf = formValue.cpf.replace(/\D/g, '');
    formValue.telefone = formValue.telefone.replace(/\D/g, '');

    this.http.post<any>('http://localhost:8080/api/users/register', formValue).subscribe({
      next: () => {
        this.isLoading = false;
        this.router.navigate(['/activate-account'], {
          queryParams: { email: formValue.email }
        });
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err.error?.message || 'Erro ao cadastrar!';
      }
    });
  }

}


