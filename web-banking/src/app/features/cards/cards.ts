import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-card-tools',
  standalone: true,
  imports: [CommonModule],
  template: `<h2>Ferramentas de Cartão</h2> <p>Aqui você gerencia seu cartão (limite, bloqueio, etc.).</p>`
})
export class CardToolsComponent {}
