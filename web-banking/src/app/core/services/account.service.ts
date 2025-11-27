import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface AccountDetails {
  agencia: string;
  conta: string;
  titular: string;
  tipo: string;
  saldo: string;
}

export interface Transaction {
  type: 'DEPOSIT' | 'WITHDRAW';
  amount: number;
  createdAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class AccountService {
  private apiUrl = 'http://localhost:8080/api/accounts'; // ajuste para seu gateway

  constructor(private http: HttpClient) {}

  // Buscar detalhes da conta
  getAccountDetails(accountNumber: string): Observable<AccountDetails> {
    return this.http.get<AccountDetails>(`${this.apiUrl}/${accountNumber}/details`);
  }

  // Buscar transações
  getAccountTransactions(accountNumber: string): Observable<Transaction[]> {
    return this.http.get<Transaction[]>(`${this.apiUrl}/${accountNumber}/transactions`);
  }
}
