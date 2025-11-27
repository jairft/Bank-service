import { Component, Input, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-success-message',
  standalone: true,
  imports: [CommonModule, MatIconModule],
  template: `
    <div class="success-message" *ngIf="show && message">
      <mat-icon color="primary" class="success-icon">check_circle</mat-icon>
      <span>{{ message }}</span>
    </div>
  `,
  styles: [`
    .success-message {
      display: flex;
      align-items: center;
      justify-content: center;
      color: #2e7d32;
      font-weight: 500;
      margin-bottom: 12px;
      background-color: #e8f5e9;
      border: 1px solid #c8e6c9;
      border-radius: 6px;
      padding: 10px;
    }

    .success-icon {
      margin-right: 8px;
    }
  `]
})
export class SuccessMessageComponent implements OnChanges {
  @Input() message: string | null = null;
  @Input() show = false;

  ngOnChanges() {
    if (this.show && this.message) {
      setTimeout(() => this.show = false, 5000); // desaparece em 5s
    }
  }
}
