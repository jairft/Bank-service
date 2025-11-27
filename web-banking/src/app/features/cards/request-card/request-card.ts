import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { Router, RouterModule } from '@angular/router';

/* Angular Material */
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

/* Componentes de mensagem */
import { ErrorMessageComponent } from '../../../shared/components/error-message/error-message';
import { SuccessMessageComponent } from '../../../shared/components/success-message/success-message';

/* Auth */
import { AuthService, User } from '../../../core/services/auth';

@Component({
  selector: 'app-request-card',
  standalone: true,
  templateUrl: './request-card.html',
  styleUrls: ['./request-card.scss'],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    HttpClientModule,
    RouterModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    ErrorMessageComponent,
    SuccessMessageComponent
  ]
})
export class RequestCardComponent {
  private fb = inject(FormBuilder);
  private http = inject(HttpClient);
  private router = inject(Router);
  private authService = inject(AuthService);

  form: FormGroup;
  isLoading = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;
  currentUser: User | null = null;

  readonly MIN_LIMIT = 3000;
  readonly MAX_LIMIT = 30000;

  brands = [
    { value: 'VISA', label: 'Visa' },
    { value: 'MASTERCARD', label: 'Mastercard' },
    { value: 'AMEX', label: 'American Express' },
    { value: 'ELO', label: 'Elo' }
  ];

  // cartões do usuário
  userCards: any[] = [];
  showCardNumbers: { [key: number]: boolean } = {}; // controla exibição por ID

  constructor() {
    this.currentUser = this.authService.currentUserValue;
    this.form = this.fb.group({
      brand: ['', [Validators.required]],
      requestedLimit: ['', [Validators.required, Validators.pattern(/^\d+(\.\d{1,2})?$/)]]
    });
  }

  ngOnInit(): void {
    if (!this.currentUser) {
      this.router.navigate(['/login']);
      return;
    }
    this.loadUserCards();
  }

  get brand() { return this.form.get('brand'); }
  get requestedLimit() { return this.form.get('requestedLimit'); }

  private getHeaders() {
    return {
      Authorization: `Bearer ${this.currentUser!.token}`,
      'X-User-Id': this.currentUser!.userId.toString()
    };
  }

  // 🔹 Carrega cartões do usuário
  loadUserCards() {
    this.isLoading = true;
    this.errorMessage = null;

    const headers = this.getHeaders();
    this.http.get<any[]>('http://localhost:8080/api/cards', { headers })
      .subscribe({
        next: (cards) => {
          this.isLoading = false;
          this.userCards = cards || [];
          // Inicializa visibilidade como “oculta”
          this.userCards.forEach(card => this.showCardNumbers[card.cardId] = false);
        },
        error: (err) => {
          this.isLoading = false;
          this.errorMessage = err.error?.message || 'Erro ao carregar cartões.';
          setTimeout(() => this.errorMessage = null, 5000);
        }
      });
  }

  // 🔹 Envia solicitação de novo cartão
  onSubmit() {
    this.errorMessage = null;
    this.successMessage = null;

    if (!this.currentUser) {
      this.errorMessage = 'Usuário não autenticado.';
      return;
    }

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = String(this.requestedLimit?.value).replace(',', '.');
    let requested = Number(raw);
    if (isNaN(requested)) {
      this.errorMessage = 'Valor de limite inválido.';
      setTimeout(() => this.errorMessage = null, 5000);
      return;
    }

    if (requested < this.MIN_LIMIT) requested = this.MIN_LIMIT;
    if (requested > this.MAX_LIMIT) requested = this.MAX_LIMIT;

    const payload = { brand: this.brand?.value, requestedLimit: requested };

    this.isLoading = true;
    this.successMessage = '⏳ Sua solicitação está em análise. Isso pode levar até 2 minutos...';

    const headers = this.getHeaders();

    this.http.post<any>('http://localhost:8080/api/cards', payload, { headers })
      .subscribe({
        next: (res) => {
          this.isLoading = false;
          this.successMessage = `✅ Cartão ${res.brand} aprovado! Limite de R$ ${res.approvedLimit.toFixed(2)}.`;
          this.form.reset({ brand: '', requestedLimit: '' });
          this.loadUserCards(); // recarrega lista após criação
          setTimeout(() => this.successMessage = null, 7000);
        },
        error: (err) => {
          this.isLoading = false;
          if (err.error?.message) this.errorMessage = err.error.message;
          else if (typeof err.error === 'string') this.errorMessage = err.error;
          else this.errorMessage = 'Erro desconhecido. Tente novamente.';
          setTimeout(() => this.errorMessage = null, 6000);
        }
      });
  }

  // 🔹 Alterna exibição dos números do cartão
  toggleCardNumber(cardId: number) {
    this.showCardNumbers[cardId] = !this.showCardNumbers[cardId];
  }

  goBack() {
    this.router.navigate(['/dashboard']);
  }
}
