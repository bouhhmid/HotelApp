import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AiInsightCardComponent } from './ai-insight-card.component';

describe('AiInsightCardComponent', () => {
  let component: AiInsightCardComponent;
  let fixture: ComponentFixture<AiInsightCardComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [AiInsightCardComponent]
    });
    fixture = TestBed.createComponent(AiInsightCardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
