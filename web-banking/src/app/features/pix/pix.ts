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
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';

/* Mask */
import { provideNgxMask } from 'ngx-mask';

/* Serviços */
import { AuthService, User } from '../../core/services/auth';

@Component({
  selector: 'app-pix',
  standalone: true,
  templateUrl: './pix.html',
  styleUrls: ['./pix.scss'],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    HttpClientModule,
    RouterModule,
    MatSnackBarModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatTabsModule,
    ErrorMessageComponent,
    SuccessMessageComponent
   
  ],
  providers: [provideNgxMask()]
})
export class PixComponent {
  private fb = inject(FormBuilder);
  private http = inject(HttpClient);
  private router = inject(Router);
  private authService = inject(AuthService);
  private snackBar = inject(MatSnackBar);

  pixKeyForm: FormGroup;
  searchForm: FormGroup;
  transferForm: FormGroup;

  selectedPixKey: any = null;
  successMessage: string | null = null;
  errorMessage: string | null = null;
  currentUser: User | null = null;

  pixKeys: any[] = [];

  constructor() {
    this.pixKeyForm = this.fb.group({
      keyType: ['', Validators.required]
    });

    this.searchForm = this.fb.group({
      keyType: ['', Validators.required],
      keyValue: ['', Validators.required]
    });

    this.transferForm = this.fb.group({
      amount: ['', [Validators.required, Validators.min(0.01)]],
      password: ['', Validators.required],
      description: ['']
    });
  }

  ngOnInit(): void {
    this.currentUser = this.authService.currentUserValue;
    if (!this.currentUser) {
      this.router.navigate(['/login']);
    } else {
      this.loadPixKeys();
    }
  }

  // -------------------
  // MÉTODO DE FORMATAÇÃO DE VALOR
  // -------------------
  formatPixAmount(): void {
    const control = this.transferForm.get('amount');
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

  // -------------------
  // MÉTODOS DE AÇÃO
  // -------------------

  private loadPixKeys(): void {
    if (!this.currentUser) return;
    const headers = { Authorization: `Bearer ${this.currentUser.token}`, 'X-User-Id': this.currentUser.userId.toString() };

    this.http.get<any[]>('http://localhost:8080/api/pix/keys', { headers })
      .subscribe({
        next: (res) => this.pixKeys = res,
        error: (err) => this.handleError(err)
      });
  }

  onRegister(): void {
    if (this.pixKeyForm.invalid || !this.currentUser) return;

    const headers = { Authorization: `Bearer ${this.currentUser.token}`, 'X-User-Id': this.currentUser.userId.toString() };
    const payload = { keyType: this.pixKeyForm.value.keyType };

    this.http.post('http://localhost:8080/api/pix/keys/register', payload, { headers })
      .subscribe({
        next: () => {
          this.successMessage = 'Chave cadastrada com sucesso!';
          this.errorMessage = null;
          this.pixKeyForm.reset({ keyType: '' });
          this.pixKeyForm.get('keyType')?.setErrors(null);
          this.snackBar.open(this.successMessage, 'OK', { duration: 3000 });
          this.loadPixKeys();
        },
        error: (err) => this.handleError(err)
      });
  }

  onDeleteKey(keyId: number): void {
    if (!this.currentUser) return;
    if (!confirm('Tem certeza que deseja excluir esta chave PIX?')) return;

    const headers = { Authorization: `Bearer ${this.currentUser.token}`, 'X-User-Id': this.currentUser.userId.toString() };

    this.http.delete(`http://localhost:8080/api/pix/keys/${keyId}`, { headers, responseType: 'text' })
      .subscribe({
        next: () => {
          this.pixKeys = this.pixKeys.filter(k => k.id !== keyId);
          this.successMessage = 'Chave PIX excluída com sucesso!';
          this.errorMessage = null;
          this.snackBar.open(this.successMessage, 'OK', { duration: 3000 });
        },
        error: (err) => this.handleError(err)
      });
  }

  onSearch(): void {
    if (this.searchForm.invalid || !this.currentUser) return;

    const headers = { Authorization: `Bearer ${this.currentUser.token}`, 'X-User-Id': this.currentUser.userId.toString() };
    const payload = { keyType: this.searchForm.value.keyType, keyValue: this.searchForm.value.keyValue };

    this.http.post('http://localhost:8080/api/pix/search', payload, { headers })
      .subscribe({
        next: (res: any) => {
          this.selectedPixKey = res;
          localStorage.setItem('pixKeyValue', res?.keyValue ?? '');
        },
        error: (err) => this.handleError(err)
      });
  }

  onTransfer(): void {
    if (this.transferForm.invalid || !this.currentUser) return;

    const keyValue = localStorage.getItem('pixKeyValue');
    if (!keyValue) {
      this.errorMessage = 'Chave de destino não definida.';
      return;
    }

    const headers = { Authorization: `Bearer ${this.currentUser.token}`, 'X-User-Id': this.currentUser.userId.toString() };
    const rawAmount = this.transferForm.value.amount;
    const amountNumber = typeof rawAmount === 'string'
      ? Number(rawAmount.toString().replace(/\./g, '').replace(',', '.'))
      : Number(rawAmount);

    const body = { amount: amountNumber, description: this.transferForm.value.description, password: this.transferForm.value.password };

    this.http.post(`http://localhost:8080/api/pix/transfer?keyValue=${encodeURIComponent(keyValue)}`, body, { headers })
      .subscribe({
        next: () => {
          this.successMessage = 'Transferência realizada com sucesso!';
          this.errorMessage = null;

          this.transferForm.reset({ amount: '', password: '', description: '' });
          this.transferForm.get('amount')?.setErrors(null);
          this.transferForm.get('password')?.setErrors(null);
          this.transferForm.get('description')?.setErrors(null);

          this.selectedPixKey = null;
          this.searchForm.reset({ keyType: '', keyValue: '' });
          this.searchForm.get('keyType')?.setErrors(null);
          this.searchForm.get('keyValue')?.setErrors(null);
          localStorage.removeItem('pixKeyValue');

         this.snackBar.open(this.successMessage, 'OK', { duration: 5000 });
         
        },
        error: (err) => this.handleError(err)
      });
  }

  clearSelected(): void {
    this.selectedPixKey = null;
    this.transferForm.reset({ amount: '', password: '', description: '' });
    this.transferForm.get('amount')?.setErrors(null);
    this.transferForm.get('password')?.setErrors(null);
    this.transferForm.get('description')?.setErrors(null);
    localStorage.removeItem('pixKeyValue');
  }

  private handleError(err: any) {
    if (err.error?.message) this.errorMessage = err.error.message;
    else if (typeof err.error === 'string') this.errorMessage = err.error;
    else this.errorMessage = 'Erro desconhecido. Tente novamente.';
    setTimeout(() => this.errorMessage = null, 5000);
  }



  goBack(): void {
    this.router.navigate(['/dashboard']);
  }
}
