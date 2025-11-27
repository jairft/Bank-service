import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface PixKey {
  id: number;
  keyType: 'CPF' | 'TELEFONE' | 'EMAIL' | 'ALEATORIO';
  keyValue: string;
  isActive: boolean;
  createdAt: string;
}

export interface PixKeyRequest {
  keyType: 'CPF' | 'TELEFONE' | 'EMAIL' | 'ALEATORIO';
}

export interface PixTransferRequest {
  keyType: 'CPF' | 'TELEFONE' | 'EMAIL' | 'ALEATORIO';
  pixKey: string;
  amount: number;
  description: string;
}

export interface PixTransferResponse {
  transactionId: string;
  status: string;
  amount: number;
  fromAccount: string;
  timestamp: string;
  message: string;
}

@Injectable({
  providedIn: 'root'
})
export class PixService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/pix';

  private getHeaders(): HttpHeaders {
    const user = JSON.parse(localStorage.getItem('currentUser') || '{}');
    return new HttpHeaders({
      'X-User-Id': user.id || ''
    });
  }

  registerPixKey(keyRequest: PixKeyRequest): Observable<PixKey> {
    return this.http.post<PixKey>(`${this.apiUrl}/keys/register`, keyRequest, { 
      headers: this.getHeaders() 
    });
  }

  getPixKeys(): Observable<PixKey[]> {
    return this.http.get<PixKey[]>(`${this.apiUrl}/keys`, { 
      headers: this.getHeaders() 
    });
  }

  transfer(transferRequest: PixTransferRequest): Observable<PixTransferResponse> {
    return this.http.post<PixTransferResponse>(`${this.apiUrl}/transfer`, transferRequest, { 
      headers: this.getHeaders() 
    });
  }

  inactivatePixKey(keyId: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/keys/${keyId}`, { 
      headers: this.getHeaders() 
    });
  }
}