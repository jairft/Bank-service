import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { HttpClient, HttpClientModule } from '@angular/common/http';

// Angular Material
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatCardModule } from '@angular/material/card';
import { MatListModule } from '@angular/material/list';
import { MatButtonModule } from '@angular/material/button';

// Serviços
import { AuthService, User } from '../../core/services/auth';

// Tipos
interface AccountDetails {
  id: number;
  titular: string;
  agencia: string;
  conta: string;
  tipo: string;
  saldo: number;
  detalhes: string;
}

interface Transaction {
  type: 'DEPOSIT' | 'PIX';
  direction?: 'IN' | 'OUT'; // Só para PIX
  amount: number;
  createdAt: string;
  description?: string;
  transactionId: string;
  pixKey?: string;
  fromUserName?: string; // Remetente
  toUserName?: string;   // Destinatário
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    HttpClientModule,
    MatToolbarModule,
    MatIconModule,
    MatMenuModule,
    MatCardModule,
    MatListModule,
    MatButtonModule
  ],
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.scss']
})
export class DashboardComponent implements OnInit {
  private http = inject(HttpClient);
  private router = inject(Router);
  private authService = inject(AuthService);

  currentUser: User | null = null;
  accountDetails: AccountDetails | null = null;
  transactions: Transaction[] = [];

  ngOnInit(): void {
    this.currentUser = this.authService.currentUserValue;

    if (!this.currentUser) {
      console.error('Usuário não logado. Redirecionando...');
      this.router.navigate(['/login']);
      return;
    }

    const userId = this.currentUser.userId;
    const token = this.currentUser.token;
    const headers = { Authorization: `Bearer ${token}` };

    // Busca informações da conta
    this.http.get<AccountDetails[]>(`http://localhost:8080/api/accounts/user/${userId}/bank-info`, { headers })
      .subscribe({
        next: (accounts) => {
          if (!accounts || accounts.length === 0) {
            console.error('Nenhuma conta encontrada para o usuário!');
            return;
          }

          const account = accounts[0]; // primeira conta
          this.accountDetails = {
            id: account['id'],
            titular: account['titular'] || this.currentUser?.nome || this.currentUser?.email || 'Usuário',
            agencia: account['agencia'],
            conta: account['conta'],
            tipo: account['tipo'],
            saldo: Number(account['saldo']?.toString().replace(/[R$\s.]/g, '').replace(',', '.')) || 0,
            detalhes: account['detalhes']
          };

          // Carrega transações
          if (this.accountDetails) {
            this.loadTransactions(this.accountDetails.id, headers);
          }
        },
        error: (err) => console.error('Erro ao buscar informações bancárias', err)
      });
  }

  loadTransactions(accountId: number, headers: any): void {
    this.http.get<Transaction[]>(`http://localhost:8080/api/accounts/${accountId}/transactions`, { headers })
      .subscribe({
        next: (txs) => {
          if (!txs) return;

          // Ordena por data decrescente e pega as últimas 6
          const sortedTxs = txs.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
          this.transactions = sortedTxs.slice(0, 6);
        },
        error: (err) => console.error('Erro ao carregar transações', err)
      });
  }

  formatCurrency(value: number | string | null | undefined): string {
    if (value == null || value === '') return 'R$ 0,00';
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL'
    }).format(Number(value));
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  updatePassword(): void {
    this.router.navigate(['/update-password']);
  }

  editProfile(): void {
  this.router.navigate(['/edit-user']);
}

}
