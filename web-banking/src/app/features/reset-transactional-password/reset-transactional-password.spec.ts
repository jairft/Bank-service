import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ResetTransactionalPassword } from './reset-transactional-password';

describe('ResetTransactionalPassword', () => {
  let component: ResetTransactionalPassword;
  let fixture: ComponentFixture<ResetTransactionalPassword>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ResetTransactionalPassword]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ResetTransactionalPassword);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
