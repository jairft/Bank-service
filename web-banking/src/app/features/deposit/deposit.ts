import { Component, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { Router, RouterModule } from '@angular/router';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { ErrorMessageComponent } from '../../shared/components/error-message/error-message';
import { SuccessMessageComponent } from '../../shared/components/success-message/success-message';

/* Angular Material */
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

/* Serviços */
import { AuthService, User } from '../../core/services/auth';

@Component({
  selector: 'app-deposit',
  standalone: true,
  templateUrl: './deposit.html',
  styleUrls: ['./deposit.scss'],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    HttpClientModule,
    RouterModule,
    MatSnackBarModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    ErrorMessageComponent,
    SuccessMessageComponent
  ]
})
export class DepositComponent {
  private fb = inject(FormBuilder);
  private http = inject(HttpClient);
  public router = inject(Router);
  private authService = inject(AuthService);

  depositForm: FormGroup;
  isLoading = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;

  currentUser: User | null = null;
  accountNumber: string | null = null;

  constructor() {
    this.depositForm = this.fb.group({
      amount: ['', [Validators.required, Validators.min(0.01)]],
      description: ['']
    });
  }

  get amount() { return this.depositForm.get('amount'); }
  get description() { return this.depositForm.get('description'); }

  ngOnInit(): void {
    this.currentUser = this.authService.currentUserValue;

    if (!this.currentUser) {
      this.router.navigate(['/login']);
      return;
    }

    const token = this.currentUser.token;
    const headers = { Authorization: `Bearer ${token}` };

    // Busca a primeira conta do usuário logado
    this.http.get<any[]>(`http://localhost:8080/api/accounts/user/${this.currentUser.userId}/bank-info`, { headers })
      .subscribe({
        next: (accounts) => {
          if (!accounts || accounts.length === 0) {
            this.errorMessage = 'Nenhuma conta encontrada para o usuário';
            return;
          }
          this.accountNumber = accounts[0].conta;
        },
        error: () => this.errorMessage = 'Erro ao buscar conta do usuário'
      });
  }

  onSubmit(): void {
  if (this.depositForm.invalid) return;

  this.isLoading = true;
  this.errorMessage = null;
  this.successMessage = null;

  const formValue = { ...this.depositForm.value };

  // Remove pontos e substitui vírgula por ponto para envio como número
  if (formValue.amount) {
    const numericAmount = formValue.amount.toString()
      .replace(/\./g, '')   // remove milhares
      .replace(',', '.');   // substitui vírgula decimal
    formValue.amount = parseFloat(numericAmount);
  }

  // Obtenha accountNumber do usuário logado
  const accountNumber = this.accountNumber;

  this.http.post(`http://localhost:8080/api/accounts/${accountNumber}/deposit`, formValue)
    .subscribe({
      next: (res: any) => {
        this.isLoading = false;
        this.successMessage = 'Depósito realizado com sucesso!';

        this.depositForm.reset({ amount: '', description: '' });
        this.amount?.setErrors(null); // remove possíveis erros antigos
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err.error?.message || 'Ocorreu um erro inesperado';
      }
    });
}


  formatDepositAmount(): void {
      const control = this.depositForm.get('amount');
      if (!control) return;

      let value = control.value;
      if (!value) return;

      // Remove tudo que não é número
      value = value.toString().replace(/\D/g, '');

      // Converte para número com centavos
      const numericValue = Number(value) / 100;

      // Formata como moeda BRL
      const formatted = new Intl.NumberFormat('pt-BR', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
      }).format(numericValue);

      // Atualiza o valor do input sem disparar novo evento
      control.setValue(formatted, { emitEvent: false });
  }


}
