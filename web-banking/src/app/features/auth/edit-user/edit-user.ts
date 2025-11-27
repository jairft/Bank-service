import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { NgxMaskDirective, NgxMaskPipe, provideNgxMask } from 'ngx-mask';

// Angular Material
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

// Serviços
import { AuthService, User } from '../../../core/services/auth';

interface UserData {
  cpf: string;
  nome: string;
  email: string;
  telefone: string;
  dataCadastro: string;
}

@Component({
  selector: 'app-edit-user',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterModule,
    HttpClientModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    NgxMaskDirective
  ],
  providers: [provideNgxMask()],
  templateUrl: './edit-user.html',
  styleUrls: ['./edit-user.scss']
})
export class EditUserComponent implements OnInit {
  private http = inject(HttpClient);
  private router = inject(Router);
  private authService = inject(AuthService);

  userData: UserData = {
    cpf: '',
    nome: '',
    email: '',
    telefone: '',
    dataCadastro: ''
  };

  loading = true;
  errorMessage = '';

  ngOnInit(): void {
    const currentUser: User | null = this.authService.currentUserValue;
    if (!currentUser) {
      this.router.navigate(['/login']);
      return;
    }

    const token = currentUser.token;
    const headers = { Authorization: `Bearer ${token}` };

    // Busca dados do usuário
    this.http.get<UserData>(`http://localhost:8080/api/users/${currentUser.userId}`, { headers })
      .subscribe({
        next: (data) => {
          // Formata CPF e telefone ao carregar
          data.cpf = this.formatCPF(data.cpf);
          data.telefone = this.formatTelefone(data.telefone);
          this.userData = data;
          this.loading = false;
        },
        error: (err) => {
          console.error(err);
          this.errorMessage = 'Erro ao carregar os dados do usuário.';
          this.loading = false;
        }
      });
  }

  updateUser(): void {
    const currentUser: User | null = this.authService.currentUserValue;
    if (!currentUser) return;

    const token = currentUser.token;
    const headers = { Authorization: `Bearer ${token}` };

    // Envia apenas email e telefone
    const payload = {
      email: this.userData.email,
      telefone: this.userData.telefone.replace(/\D/g, '') // remove máscara antes de enviar
    };

    this.http.put<UserData>(`http://localhost:8080/api/users/update/${currentUser.userId}`, payload, { headers })
      .subscribe({
        next: (data) => {
          alert('Dados atualizados com sucesso!');
        },
        error: (err) => {
          console.error(err);
          this.errorMessage = 'Erro ao atualizar os dados.';
        }
      });
  }

  cancel(): void {
    this.router.navigate(['/dashboard']);
  }

  // --- Funções de formatação ---
  formatCPF(cpf: string): string {
    if (!cpf) return '';
    return cpf.replace(/^(\d{3})(\d{3})(\d{3})(\d{2})$/, '$1.$2.$3-$4');
  }

  formatTelefone(tel: string): string {
    if (!tel) return '';
    if (tel.length === 11) {
      return tel.replace(/^(\d{2})(\d{5})(\d{4})$/, '($1) $2-$3');
    }
    return tel;
  }

  goBack(): void {
    this.router.navigate(['/dashboard']);
  }
}
