import { Component } from '@angular/core';
import { FieldArrayType } from '@ngx-formly/core';

@Component({
    selector: 'formly-repeat-section',
    templateUrl: './repeat.html'
})
export class RepeatTypeComponent extends FieldArrayType {

    public add(i?: number, initialModel?: any): void {
        super.add(i, initialModel);
        this.formControl.markAsDirty();
    }

    public remove(i: number): void {
        super.remove(i);
        this.formControl.markAsDirty();
    }
}