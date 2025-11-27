import { Component, Input, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-error-message',
  standalone: true,
  imports: [CommonModule, MatIconModule],
  template: `
    <div class="error-message" *ngIf="show && message">
      <mat-icon color="warn" class="error-icon">error_outline</mat-icon>
      <span>{{ message }}</span>
    </div>
  `,
  styles: [`
    .error-message {
      display: flex;
      align-items: center;
      justify-content: center;
      color: #d32f2f;
      font-weight: 500;
      margin-bottom: 12px;
      background-color: #fdecea;
      border: 1px solid #f5c6cb;
      border-radius: 6px;
      padding: 10px;
    }

    .error-icon {
      margin-right: 8px;
    }
  `]
})
export class ErrorMessageComponent implements OnChanges {
  @Input() message: string | null = null;
  @Input() show = false;

  ngOnChanges() {
    if (this.show && this.message) {
      setTimeout(() => this.show = false, 8000); // desaparece em 5s
    }
  }
}
