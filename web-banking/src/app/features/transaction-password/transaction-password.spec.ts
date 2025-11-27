import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TransactionalPasswordComponent } from './transaction-password';

describe('TransactionPassword', () => {
  let component: TransactionalPasswordComponent;
  let fixture: ComponentFixture<TransactionalPasswordComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TransactionalPasswordComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TransactionalPasswordComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
